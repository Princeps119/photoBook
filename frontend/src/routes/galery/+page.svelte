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
    const photoId = '698ddae0db2a9d6f50a6ccd9';
    try {
        const response = await fetch(`/api/photos/${photoId}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            }
        });
        
        if (!response.ok) {
            throw new Error(`Error: ${response.status}`);
        }
        
        const photoData = await response.json();
        console.log('Photo Data Response:', photoData);
    } catch (error) {
        console.error('Error fetching photo:', error);
    }
    console.log(`Photo clicked: ${photoId}`);
}
</script>
<main>
    <div>
        <span>{photos.length} Photos</span>
        {#each photos as photo}
            <div>
                <h2>{photo.title}</h2>
                <img src={photo.url} alt={photo.title} />
            </div>
        {/each}
    </div>
    <div>
        <button onclick={handleClick}>Fetch Photo Data</button>
    </div>
</main>