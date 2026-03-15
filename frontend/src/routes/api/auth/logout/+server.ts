import type { RequestHandler } from './$types';

export const POST: RequestHandler = async ({ request, locals, cookies }) => {
	try {
		// Clear the authentication cookie

		const response = await fetch('http://localhost:8080/api/logout', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
            },
            body: JSON.stringify(locals.token),
		});
		if (!response.ok) {
			return new Response(JSON.stringify({ success: false, message: 'Logout failed' }), {
				status: 500,
				headers: { 'Content-Type': 'application/json' }
			});
		} else {
			cookies.delete('token', { path: '/' });
            locals.token = null;
            console.log('Logout successful, token cleared', locals.token);

			return new Response(JSON.stringify({ success: true, message: 'Logged out successfully' }), {
				status: 200,
				headers: { 'Content-Type': 'application/json' }
			});
		}
	} catch (error) {
		console.error('Logout error:', error);
		return new Response(JSON.stringify({ success: false, message: 'Logout failed' }), {
			status: 500,
			headers: { 'Content-Type': 'application/json' }
		});
	}
};
