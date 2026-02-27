import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';

const BASE_URL = 'http://localhost:8080';

export const GET: RequestHandler = async ({ params, locals }) => {
    const id = params.id;
    const token = locals.token;

    if (!token) {
        return json({ error: 'Unauthorized' }, { status: 401 });
    }

    console.log(`POST request for photo ID: ${id}`);

    try {
        const response = await fetch(`${BASE_URL}/api/image/findid?id=${id}&tag=Public`, {
            method: 'GET',
            headers: {
                Authorization: "Bearer " + JSON.stringify(locals.token),
            }
        });

        console.log(`API Response Status: ${response.status}`);
        if (!response.ok) {
            throw new Error(`API Error: ${response.status} ${response.statusText}`);
        }

        const data = await response.json();
        const base64String = data.body || data;
        const dataUrl = `data:image/jpeg;base64,${base64String}`;

        return json({ id, imageUrl: dataUrl });
    } catch (error) {
        console.error(`Error loading photo with id ${id}:`, error);
        return json({ error: 'Failed to load photo' }, { status: 500 });
    }
};