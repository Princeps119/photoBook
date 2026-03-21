import type { PageServerLoad } from "./$types";


export const load: PageServerLoad = async () => {
	let photos = [];

	try {
		const photoidresponse = await fetch('http://localhost:8080/api/image/allpublic', {
			method: 'GET',
			headers: {
				'Content-Type': 'application/json'
			}
		});
		if (!photoidresponse.ok) {
			console.error(`API Error: ${photoidresponse.status} ${photoidresponse.statusText}`);
			const errorText = await photoidresponse.text();
			console.error(`API Error details: ${errorText}`);
			throw new Error(`API Error: ${photoidresponse.status} ${photoidresponse.statusText}`);
		}

		const photoIds = await photoidresponse.json();

		photos = photoIds.body || photoIds || [];
	} catch (error) {
		console.error('Failed to load photo IDs:', error);
	}

	return {
		photos
	};
};
