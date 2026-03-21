import {json} from '@sveltejs/kit';
import type {RequestHandler} from './$types';

const BASE_URL = 'http://localhost:8080';

export const GET: RequestHandler = async ({ params, locals, url }) => {
    const id = params.id;
    const token = locals.token;
    const isPublic = url.searchParams.get('tag') === 'Public';
    const isPublicPhotos = url.searchParams.get('type') === 'Public';
    
    const backendUrl = isPublicPhotos ? `${BASE_URL}/api/image/getpublicimage?id=${id}&tag=Public` 
    : `${BASE_URL}/api/image/findid?id=${id}&tag=${isPublic ? 'Public' : 'Private'}`;
    
    if (!token && !isPublicPhotos) {
        return json({ error: 'Unauthorized' }, { status: 401 });
    }

    try {
        const url = backendUrl;
        const response = await fetch(url, {
            method: 'GET',
            headers: {
            Authorization: "Bearer " + JSON.stringify(locals.token),
    },
        });

        const responseText = await response.text();

        if (!response.ok) {
            console.error(`API Error Response: ${responseText}`);
            throw new Error(`API Error: ${response.status} ${response.statusText}`);
        }

        const data = JSON.parse(responseText);
        const base64String = data.base64.base64;
        
        if (!base64String) {
            console.error('No base64 data found in API response');
            return json({ error: 'Invalid image data' }, { status: 500 });
        }

        const buffer = Buffer.from(base64String, 'base64');
        const contentType = data.metadata?.contentType || 'image/png';

        return new Response(buffer, {
            headers: {
                'Content-Type': contentType,
                'Cache-Control': 'public, max-age=3600'
            }
        });
    } catch (error) {
        console.error(`Error loading photo with id ${id}:`, error);
        return json({ error: 'Failed to load photo' }, { status: 500 });
    }
};