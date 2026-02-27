<script lang="ts">
	import { onMount } from 'svelte';
	import { api } from '../../apis/api';

   let { data } = $props();
   let photos = $derived(data.photos || []);

   
//    onMount(async() => {
//     console.log('onMount called, fetching photos...', photos);
//     try {
//             // for (const photo of photos) {
//             //     console.log(`Fetching data for photo ID: ${photo.hexStringId}`);
//             //     const photoData = await api.Photos.get(photo.hexStringId);
//             //     console.log(`Photo Data for ID ${photo.hexStringId}:`, photoData);
//             // }
//             const photoData = await api.Photos.get('698ddae0db2a9d6f50a6ccd9');
//             console.log('Photo Data for ID 698ddae0db2a9d6f50a6ccd9:', photoData);

           
           
           
            
//         } catch (error) {
//            console.error('Error fetching photos:', error);
//         } finally {
//             console.log('Finished fetching photos');
//         }
//     console.log('Data from server:', data);
//     console.log('Photos from server:', data.photos);
//    });

//    console.log('Photos:', photos);

async function handleClick() {
    try {
        const photoidresponse = await fetch('http://localhost:8080/api/image/allforuser', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                // This might fail in the browser if token is only in HttpOnly cookies
                // But this is just for manual testing in the UI
            }
        });
        console.log('Manual fetch status:', photoidresponse.status);
    } catch (error) {
        console.error('Error fetching photo:', error);
    }
}
</script>
<main>
    <div>
        <span>{photos.length} Photos</span>
        {#each photos as photo}
            <div>
                <h2>{photo.filename}</h2>
                <img src="/api/photos/{photo.hexStringId}" alt={photo.filename} style="max-width: 300px;" />
            </div>
        {/each}
    </div>
    <div>
        <button onclick={handleClick}>Fetch Photo Data</button>
    </div>
</main>