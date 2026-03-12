<script lang="ts">
	import LoadingSpinner from '$lib/components/ui/common/Loading-Spinner.svelte';
	import Pagination from '$lib/components/ui/common/pagination.svelte';
	import PhotoCard from '$lib/components/ui/common/photo-card.svelte';
	import PhotoModal from '$lib/components/ui/common/Photo-Modal.svelte';
	import type { PhotoData } from '$lib/types/types.js';

	let { data } = $props();
	let photos = $derived(data.photos || []);
	let isPublic: boolean = $state(false);

	const PHOTOS_PER_PAGE = 8;
	let currentPage = $state(1);
	let isLoading = $state(false);

	let totalPages = $derived(Math.ceil(photos.length / PHOTOS_PER_PAGE));
	let paginatedPhotos: PhotoData[] = $derived(
		photos.slice((currentPage - 1) * PHOTOS_PER_PAGE, currentPage * PHOTOS_PER_PAGE)
	);

	function handlePageChange(page: number) {
		isLoading = true;
		currentPage = page;
		setTimeout(() => {
			isLoading = false;
		}, 300);
	}

	/* 	async function handleClick() {
		try {
			const photoidresponse = await fetch('http://localhost:8080/api/image/allforuser', {
				method: 'GET',
				headers: {
					'Content-Type': 'application/json'
				}
			});
			console.log('Manual fetch status:', photoidresponse.status);
		} catch (error) {
			console.error('Error fetching photo:', error);
		}
	} */

let photoUrl = $state('');
let isModalOpen = $state(false);
function openPhotoModal(photo: PhotoData) {
    photoUrl = `/api/photos/load/${photo.hexStringId}?tag=${photo.metadata.tag}`;
    isModalOpen = true;
}
</script>
<PhotoModal bind:isOpen={isModalOpen} imageSrc={photoUrl} imageAlt="Photo Modal" />

<main>
	<div class="header">
		<h1>Öffentliche Bilder:</h1>
		<span>{photos.length} {photos.length === 1 ? 'Bild' : 'Bilder'}</span>
		<!-- <button class="fetch-btn" onclick={handleClick}>Fetch Photo Data</button> -->
	</div>

	<div class="content">
		{#if isLoading}
			<LoadingSpinner />
		{:else}
			<div class="grid" class:loading={isLoading}>
				{#each paginatedPhotos as photo}
					<PhotoCard
						src={`/api/photos/load/${photo.hexStringId}?tag=Public`}
						alt={photo.filename}
						title={photo.filename}
						description={photo.metadata.tag}
						onClick={() => openPhotoModal(photo)}
					/>
				{/each}
			</div>
		{/if}
	</div>

	<Pagination {currentPage} {totalPages} onPageChange={handlePageChange} />
</main>

<style>
	main {
		max-width: 1200px;
		margin: 0 auto;
		padding: 2rem 1rem;
	}

	.header {
		display: flex;
        flex-direction: row;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 2rem;
	}

	.fetch-btn {
		background: #0070f3;
		color: #fff;
		border: none;
		padding: 0.7rem 1.5rem;
		border-radius: 6px;
		cursor: pointer;
		font-size: 1rem;
		transition: background 0.2s;
	}

	.fetch-btn:hover {
		background: #005bb5;
	}

	.grid {
		display: grid;
		grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
		gap: 2rem;
		margin-bottom: 2rem;
		opacity: 1;
		transition: opacity 0.2s;
	}

	.grid.loading {
		opacity: 0.5;
		pointer-events: none;
	}

	.content {
		min-height: 600px;
	}
</style>
