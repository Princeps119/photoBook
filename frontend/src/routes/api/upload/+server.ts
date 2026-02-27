import type { RequestHandler } from '@sveltejs/kit';

const BASE_URL = 'http://localhost:8080';

export const POST: RequestHandler = async ({ request, locals }) => {
  try {
    const formData = await request.formData();
    
    const file = formData.get('file') as File;
    const title = formData.get('title') as string;
    const description = formData.get('description') as string;
    const type = formData.get('type') as 'Public' | 'Private';
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
    // const base64Data = Buffer.from(buffer).toString('base64');
    const base64Mock = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";

    const body = {
      filename: title,
      metadata: {
        tag: type,
      },
      image: {
        base64: base64Mock,
      }
    };

    console.log('FILE:', title);
    console.log("locals:", locals);
    // console.log('BODY:', JSON.stringify(body));
    console.log('TOKEN:', locals.token);

  //   return new Response(
  //   JSON.stringify({ message: 'test' }),
  //   { status: 200, headers: { 'Content-Type': 'application/json' } }
  // );

    const response = await fetch(`${BASE_URL}/api/image/save`, {
      method: 'POST',
      headers: {
      "Content-Type": "application/json",
      Authorization: "Bearer " + JSON.stringify(locals.token),
    },
      body: JSON.stringify(body),
    });

    console.log('API Token for Response Header:', JSON.stringify(locals.token));
    if (!response.ok) {
      throw new Error(`API Error: ${response.status} ${response.statusText}`);
    }

    console.log('API Response Status:', response.status);
    const result = response;
    if (response.ok) {
      return new Response(JSON.stringify(result), {
      status: 200,
      headers: { 'Content-Type': 'application/json' },
    });
    } else {
      return new Response(
        JSON.stringify({ error: 'Fehler beim Hochladen' }),
        { status: 500, headers: { 'Content-Type': 'application/json' } }
      );
    }

    
  } catch (error) {
    console.error('Upload-Fehler:', error);
    return new Response(
      JSON.stringify({
        error: error instanceof Error ? error.message : 'Upload fehlgeschlagen',
      }),
      { status: 500, headers: { 'Content-Type': 'application/json' } }
    );
  }
  // } catch (error) {
  //   console.error('Upload-Fehler:', error);
  //   return new Response(
  //     JSON.stringify({
  //       error: error instanceof Error ? error.message : 'Upload fehlgeschlagen',
  //     }),
  //     { status: 200, headers: { 'Content-Type': 'application/json' } }
  //   );
  // }
  
};