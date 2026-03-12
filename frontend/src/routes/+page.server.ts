import type { PageServerLoad } from "./$types";


export const load: PageServerLoad = async () => {
	let photos = [];

    console.log('Public picture loading function called :');

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
		console.log('Received photo IDs:', JSON.stringify(photoIds));

		photos = photoIds.body || photoIds || [];
		//    console.log('Photos array:', photos);
		// Für jede ID den photoresponse aufrufen
		// for (const id of photoIds) {
		//     try {
		//         const photoresponse = await fetch(`http://localhost:8080/api/image/findid?id=${id}&tag=Public`, {
		//             method: 'GET',
		//             headers: {
		//                 'Content-Type': 'application/json',
		//                 'Authorization': `Bearer ${token}`
		//             }
		//         });
		//         if (photoresponse.ok) {
		//             const photoData = await photoresponse.json();
		//             photos.push(photoData);
		//             console.log('Photo API Response Status:', photoresponse.status);
		//         } else {
		//             console.error(`Failed to load photo with id ${id}: ${photoresponse.status}`);
		//         }
		//     } catch (error) {
		//         console.error(`Error loading photo with id ${id}:`, error);
		//     }
		// }
	} catch (error) {
		console.error('Failed to load photo IDs:', error);
	}

	return {
		photos
	};
};
