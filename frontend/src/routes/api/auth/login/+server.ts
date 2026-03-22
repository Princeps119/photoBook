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

export const POST: RequestHandler = async ({ request, locals, cookies }) => {
  try {
    const body: LoginRequest = await request.json();
    if (!body.mail || !body.password) {
      return new Response(
        JSON.stringify({ success: false, message: 'Email and password are required' }),
        {
          status: 400,
          headers: { 'Content-Type': 'application/json' }
        }
      );
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(body.mail)) {
      return new Response(
        JSON.stringify({ success: false, message: 'Invalid email format' }),
        {
          status: 400,
          headers: { 'Content-Type': 'application/json' }
        }
      );
    }

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
      return new Response(
        JSON.stringify({
          success: false,
          message: errorData.message || 'Login failed. Please check your credentials.',
        }),
        {
          status: response.status,
          headers: { 'Content-Type': 'application/json' }
        }
      );
    }

    const responseData: TokenData = await response.json();
    let username : string = responseData.username || 'Unknown User';
    if(responseData && responseData.username) {
      username = responseData.username;
    }

    cookies.set('token', JSON.stringify(responseData), {
    httpOnly: true,
    secure: true,
    sameSite: 'strict',
    path: '/',
    maxAge: 60 * 60 * 4, // 4 Stunden wegen Backend
  });

    return new Response(
      JSON.stringify({
        success: true,
        message: 'Login successful',
        username: username,
      }),
      { status: 200 }
    );
  } catch (error) {
    console.error('Login error:', error);
    return new Response(
      JSON.stringify({
        success: false,
        message: 'An error occurred during login. Please try again.',
      }),
      { status: 500 }
    );
  }
};