import { redirect, type Handle } from '@sveltejs/kit';

const protectedPaths = ['/api/upload', '/upload', '/galery'];

export const handle: Handle = async ({ event, resolve }) => {
	const tokenString = event.cookies.get('token');
	if (tokenString) {
		event.locals.token = JSON.parse(tokenString);
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
