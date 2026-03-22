import { redirect, type Handle } from '@sveltejs/kit';

const protectedPaths = ['/api/upload', '/upload', '/user-gallery'];

export const handle: Handle = async ({ event, resolve }) => {
	const tokenString = event.cookies.get('token');
	if (tokenString) {
		const token = JSON.parse(tokenString);
		event.locals.token = token;
		event.locals.isAuthenticated = true;
		event.locals.username = token.username;
	} else {
		event.locals.token = null;
		event.locals.isAuthenticated = false;
		event.locals.username = undefined;
	}

  const { pathname } = event.url;

	const isProtected = protectedPaths.some((path) =>
		pathname.startsWith(path)
	);

	if (isProtected) {
		const token = event.locals.token;
		if (!token) {
			throw redirect(303, '/login');
		}
	}
	return resolve(event);
};
