<script lang="ts">
	import { invalidateAll } from '$app/navigation';
	import LoadingSpinner from '$lib/components/ui/common/Loading-Spinner.svelte';
	import Pagination from '$lib/components/ui/common/Pagination.svelte';
	import PhotoModal from '$lib/components/ui/common/Photo-Modal.svelte';
	import PhotoPreviewCard from '$lib/components/ui/common/Photo-Preview-Card.svelte';
	import PhotoViewCard from '$lib/components/ui/common/PhotoView-Card.svelte';
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

	let photoUrl = $state('');
	let isModalOpen = $state(false);
	function openPhotoModal(photo: PhotoData) {
		photoUrl = `/api/photos/load/${photo.hexStringId}?tag=${photo.metadata.tag}`;
		isModalOpen = true;
	}
	async function deletePhoto(photo: PhotoData) {
		try {
			const response = await fetch(`/api/photos/delete/${photo.hexStringId}`, {
				method: 'DELETE',
				headers: {
					'Content-Type': 'application/json'
				}
			});
			if (response.ok) {
				invalidateAll();
			} else {
				console.error('Failed to delete photo');
			}
		} catch (error) {
			console.error('Error deleting photo:', error);
		}
	}
</script>

<PhotoModal bind:isOpen={isModalOpen} imageSrc={photoUrl} imageAlt="Photo Modal" />

<main>
	<div class="">
		<div class="header">
			<h1>Meine Bilder</h1>
			<h1>{photos.length} {photos.length === 1 ? 'Bild' : 'Bilder'}</h1>
		</div>
	</div>

	<div class="content">
		{#if isLoading}
			<LoadingSpinner />
		{:else if paginatedPhotos.length === 0}
			<div class="no-content">
				<p>Keine Bilder gefunden.</p>
				<p>Füge Bilder hinzu, indem du auf <a id="uploadlink" href="/upload">"Hochladen"</a> klickst.</p>
			</div>
		{:else}
			<div class="grid" class:loading={isLoading}>
				{#each paginatedPhotos as photo}
					<PhotoPreviewCard
						src={`/api/photos/load/${photo.hexStringId}?tag=${photo.metadata.tag}`}
						alt={photo.filename}
						title={photo.filename}
						description={photo.metadata.tag}
						onClick={() => openPhotoModal(photo)}
						onDelete={() => deletePhoto(photo)}
						canDelete={true}
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

	h1 {
		font-size: 2rem;
		margin-bottom: 10px;
		color: white;
	}

	.no-content {
		text-align: center;
		color: white;
		font-size: 1.2rem;
	}

	#uploadlink {
		color: #F59E0B;
		font-weight: 600;
	}
</style>
