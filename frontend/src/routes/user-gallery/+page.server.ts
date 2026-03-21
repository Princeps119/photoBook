import type {PageServerLoad} from "./$types";

export const load: PageServerLoad = async (event) => {
    const token = event.locals.token;    
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
    }
    
    return {
        photos
    };
}