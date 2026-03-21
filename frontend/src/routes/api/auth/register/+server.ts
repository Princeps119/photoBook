import { json, type RequestHandler } from '@sveltejs/kit';

export const POST: RequestHandler = async ({ request, locals, cookies }) => {
	try {
		const body = await request.json();

		if (!body.username || !body.email || !body.password) {
			return new Response(
				JSON.stringify({ success: false, message: 'Username, email and password are required' }),
				{ status: 400, headers: { 'Content-Type': 'application/json' } }
			);
		}

		const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
		if (!emailRegex.test(body.email)) {
			return new Response(
				JSON.stringify({ success: false, message: 'Invalid email format' }),
				{ status: 400, headers: { 'Content-Type': 'application/json' } }
			);
		}

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


		if (response.ok) {
            return new Response(
				JSON.stringify({
					success: true,
					message: 'Registration successful'
				}),
				{ status: 200 }
			);	
		} else {
            const errorData = await response.json();
			return new Response(
				JSON.stringify({
					success: false,
					message: errorData.message || 'Register failed. Please check your credentials.'
				}),
				{
					status: response.status,
					headers: { 'Content-Type': 'application/json' }
				}
			);
		}
	} catch (error) {
		console.error('Register error:', error);
		return new Response(
			JSON.stringify({
				success: false,
				message: 'An error occurred during registration. Please try again.'
			}),
			{ status: 500 }
		);
	}
};
