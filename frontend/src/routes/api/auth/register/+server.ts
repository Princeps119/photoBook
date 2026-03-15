import { json, type RequestHandler } from '@sveltejs/kit';

export const POST: RequestHandler = async ({ request, locals, cookies }) => {
	try {
		// Parse the incoming request body
		const body = await request.json();

		console.log('Login Request Body:', body);
		// Validate required fields
		if (!body.username || !body.email || !body.password) {
			return new Response(
				JSON.stringify({ success: false, message: 'Username, email and password are required' }),
				{
					status: 400,
					headers: { 'Content-Type': 'application/json' }
				}
			);
		}

		// Validate email format
		const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
		if (!emailRegex.test(body.email)) {
			return new Response(JSON.stringify({ success: false, message: 'Invalid email format' }), {
				status: 400,
				headers: { 'Content-Type': 'application/json' }
			});
		}

		// Forward the request to the backend API
		const response = await fetch('http://localhost:8080/api/register', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify({
				username: body.username,
				mail: body.email,
				password: body.password
			})
		});

        console.log('API Response Status:', response.status, response.statusText);

		if (response.ok) {
            return json(
				{
					success: true,
					message: 'Registration successful'
				},
				{ status: 200 }
			);	
		} else {
            const errorData = await response.json();
			return json(
				{
					success: false,
					message: errorData.message || 'Register failed. Please check your credentials.'
				},
				{ status: response.status }
			);
		}
	} catch (error) {
		console.error('Register error:', error);
		return json(
			{
				success: false,
				message: 'An error occurred during registration. Please try again.'
			},
			{ status: 500 }
		);
	}
};
