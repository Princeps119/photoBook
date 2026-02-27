import type { PageServerLoad } from "./$types";

export const load: PageServerLoad = async (event) => {
    const token = event.locals.token;
    console.log('Load function called with token:', token);
    
    let photos = [];
    
    if (token) {
        try {
            const photoidresponse = await fetch('http://localhost:8080/api/image/allforuser', {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: "Bearer " + JSON.stringify(token),
                }
            });
            if (!photoidresponse.ok) {
                throw new Error(`API Error: ${photoidresponse.status} ${photoidresponse.statusText}`);
            }
            
           const photoIds = await photoidresponse.json();
            
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
    }
    
    return {
        photos
    };
}