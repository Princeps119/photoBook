import type { RequestHandler } from '@sveltejs/kit';

const BASE_URL = 'http://localhost:8080/api';

export const POST: RequestHandler = async ({ request, locals }) => {
  try {
    const formData = await request.formData();
    
    const file = formData.get('file') as File;
    const title = formData.get('title') as string;
    const description = formData.get('description') as string;
    const type = formData.get('type') as 'public' | 'private';
    const userEmail = formData.get('userEmail') as string;


    // if (!file || !title || !userEmail) {
    //   return new Response(
    //     JSON.stringify({ error: 'Fehlende erforderliche Felder' }),
    //     { status: 400, headers: { 'Content-Type': 'application/json' } }
    //   );
    // }

    // if (!locals.token) {
    //   return new Response(
    //     JSON.stringify({ error: 'Nicht authentifiziert' }),
    //     { status: 401, headers: { 'Content-Type': 'application/json' } }
    //   );
    // }

    const buffer = await file.arrayBuffer();
    const base64Data = Buffer.from(buffer).toString('base64');

    const body = {
      filename: title,
      metadata: {
        tag: type,
      },
      image: {
        data: base64Data,
      }
    };

    console.log('FILE:', file.name);
    console.log('BODY:', JSON.stringify(body));

    return new Response(
    JSON.stringify({ message: 'test' }),
    { status: 200, headers: { 'Content-Type': 'application/json' } }
  );

    // API aufrufen
//     const response = await fetch(`${BASE_URL}/photos`, {
//       method: 'POST',
//       headers: {
//         'Content-Type': 'application/json',
//         'Authorization': `Bearer ${locals.token}`,
//       },
//       body: JSON.stringify(body),
//     });

//     if (!response.ok) {
//       throw new Error(`API Error: ${response.status} ${response.statusText}`);
//     }

//     const result = await response.json();

//     return new Response(JSON.stringify(result), {
//       status: 200,
//       headers: { 'Content-Type': 'application/json' },
//     });
//   } catch (error) {
//     console.error('Upload-Fehler:', error);
//     return new Response(
//       JSON.stringify({
//         error: error instanceof Error ? error.message : 'Upload fehlgeschlagen',
//       }),
//       { status: 500, headers: { 'Content-Type': 'application/json' } }
//     );
//   }
  } catch (error) {
    console.error('Upload-Fehler:', error);
    return new Response(
      JSON.stringify({
        error: error instanceof Error ? error.message : 'Upload fehlgeschlagen',
      }),
      { status: 200, headers: { 'Content-Type': 'application/json' } }
    );
  }
  
};