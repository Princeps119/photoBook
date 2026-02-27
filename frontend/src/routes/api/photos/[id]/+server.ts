import {json} from '@sveltejs/kit';
import type {RequestHandler} from './$types';

const BASE_URL = 'http://localhost:8080';

export const GET: RequestHandler = async ({ params, locals }) => {
    const id = params.id;
    const token = locals.token;

    if (!token) {
        return json({ error: 'Unauthorized' }, { status: 401 });
    }

    console.log(`GET request for photo ID: ${id}`);
    console.log(`Using token: ${JSON.stringify(locals.token)}`);

    try {
        const url = `${BASE_URL}/api/image/findid?id=${id}&tag=Public`;
        console.log(`Fetching from URL: ${url}`);
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                Authorization: "Bearer " + JSON.stringify(locals.token),
            }
        });

        console.log(`API Response Status: ${response.status} ${response.statusText}`);
        const responseText = await response.text();
        console.log(`API Response Text (first 500 chars): ${responseText.substring(0, 500)}`);

        if (!response.ok) {
            console.error(`API Error Response: ${responseText}`);
            throw new Error(`API Error: ${response.status} ${response.statusText}`);
        }

        const data = JSON.parse(responseText);
        console.log('API Data received:', JSON.stringify(data).substring(0, 100) + '...');
        const base64String = data.base64 || data.body || (data.image ? data.image.base64 : null) || data;
        
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