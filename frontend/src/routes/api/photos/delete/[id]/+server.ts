import type { RequestHandler } from "./$types";

export const DELETE: RequestHandler = async ({ params, locals }) => {
    const { id } = params;

    if (!locals.token) {
        return new Response(
            JSON.stringify({ error: 'Nicht authentifiziert' }),
            { status: 401, headers: { 'Content-Type': 'application/json' } }
        );
    }

    try {
        const response = await fetch(`http://localhost:8080/api/image/delete/${id}`, {
            method: 'DELETE',
            headers: {
                Authorization: "Bearer " + JSON.stringify(locals.token),
            },
        });

        if (!response.ok) {
            const errorData = await response.json();
            return new Response(
                JSON.stringify({ error: errorData.message || 'Fehler beim Löschen des Fotos' }),
                { status: response.status, headers: { 'Content-Type': 'application/json' } }
            );
        }

        return new Response(
            JSON.stringify({ message: 'Foto erfolgreich gelöscht' }),
            { status: 200, headers: { 'Content-Type': 'application/json' } }
        );
    } catch (error) {
        return new Response(
            JSON.stringify({ error: 'Serverfehler beim Löschen des Fotos' }),
            { status: 500, headers: { 'Content-Type': 'application/json' } }
        );
    }
};