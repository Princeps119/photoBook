import { json, type RequestHandler } from '@sveltejs/kit';

interface LoginRequest {
  mail: string;
  password: string;
}

interface TokenData {
  username: string;
  encryptedMail: string;
  timestamp: string;
  version: string;
}

interface LoginResponse {
  success: boolean;
  message: string;
  token?: TokenData;
}

export const POST: RequestHandler = async ({ request, locals, cookies }) => {
  try {
    // Parse the incoming request body
    const body: LoginRequest = await request.json();

    console.log('Login Request Body:', body);
    // Validate required fields
    if (!body.mail || !body.password) {
      return json(
        {
          success: false,
          message: 'Email and password are required',
        },
        { status: 400 }
      );
    }

    // Validate email format
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(body.mail)) {
      return json(
        {
          success: false,
          message: 'Invalid email format',
        },
        { status: 400 }
      );
    }

    // Forward the request to the backend API
    const response = await fetch('http://localhost:8080/api/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        mail: body.mail,
        password: body.password,
      }),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      return json(
        {
          success: false,
          message: errorData.message || 'Login failed. Please check your credentials.',
        },
        { status: response.status }
      );
    }

    const responseData: TokenData = await response.json();
    let username : string = responseData.username || 'Unknown User';
    if(responseData && responseData.username) {
      username = responseData.username;
    }

    // Store the token in cookies and locals
    cookies.set('token', JSON.stringify(responseData), {
    httpOnly: true,
    secure: true,
    sameSite: 'strict',
    path: '/',
    // maxAge: 60 * 60 * 24 * 7, // 7 Tage
    maxAge: 10, // 10 Sekunden für Testzwecke
  });
    console.log('Login successful, token stored in locals:', responseData);
    console.log("Locals.token : ", locals.token);

    return json(
      {
        success: true,
        message: 'Login successful',
        username: username,
      },
      { status: 200 }
    );
  } catch (error) {
    console.error('Login error:', error);
    return json(
      {
        success: false,
        message: 'An error occurred during login. Please try again.',
      },
      { status: 500 }
    );
  }
};