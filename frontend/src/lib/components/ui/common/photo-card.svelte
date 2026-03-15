<script lang="ts">
	import Fa from 'svelte-fa';
	import { faTrash } from '@fortawesome/free-solid-svg-icons';
	import AreYouSureDialog from './Are-You-Sure-Dialog.svelte';

	type Props = {
		src: string;
		alt?: string;
		title?: string;
		description?: string;
		canDelete?: boolean;
		onClick?: (event: MouseEvent) => void;
		onDelete?: () => void;
	};

	let {
		src,
		alt = 'Photo',
		title = 'Photo Title',
		description = 'Photo description goes here.',
		canDelete = false,
		onClick,
		onDelete
	}: Props = $props();
	let openModal = $state(false);
	function openModalHandler(event: MouseEvent) {
		event.stopPropagation();
		openModal = true;
	}
</script>

<AreYouSureDialog
	bind:isOpen={openModal}
	title="Foto löschen"
	message="Möchten Sie dieses Foto wirklich löschen? Dieser Vorgang kann nicht rückgängig gemacht werden."
	confirmText="Löschen"
	cancelText="Abbrechen"
	onConfirm={onDelete}
/>

<!-- svelte-ignore a11y_no_static_element_interactions -->
<!-- svelte-ignore a11y_click_events_have_key_events -->
<div class="mainCard" onclick={onClick}>
	<img {src} {alt} class="photo" />
	<div class="photoInfo">
		<h2>{title}</h2>
		<p>Typ: {description}</p>
		{#if canDelete}
			<button id="deleteBtn" onclick={(event: MouseEvent) => openModalHandler(event)}><Fa icon={faTrash} /></button
			>
		{/if}
	</div>
</div>

<style>
	.mainCard {
		display: flex;
		flex-direction: column;
		align-items: center;
		background-color: #f59e0b;
		width: 250px;
		height: 370px;
		border-radius: 8px;
		box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
		padding: 16px;
		margin: 16px auto;
		flex-shrink: 0;
		transition:
			transform 0.3s ease,
			box-shadow 0.3s ease;
		cursor: pointer;
	}

	.mainCard:hover {
		transform: translateY(-4px);
	}

	.photo {
		width: 250px;
		height: 250px;
		object-fit: cover;
		border-radius: 4px;
		flex-shrink: 0;
	}

	.photoInfo {
		color: #1E293B;
		text-align: center;
		margin-top: 12px;
		width: 100%;
	}

	.photoInfo h2 {
		font-size: 1rem;
		font-weight:600;
		color: #1e293b;
		margin-bottom: 8px;
		margin: 0;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.photoInfo p {
		font-size: 1rem;
		color: #1e293b;
	}

	#deleteBtn {
		cursor: pointer;
		color: red;
	}

	#deleteBtn:hover {
		scale: 1.2;
		color: darkred;
	}
</style>
