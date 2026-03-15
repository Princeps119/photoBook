<script lang="ts">
	import { faUser } from '@fortawesome/free-solid-svg-icons';
	import Fa from 'svelte-fa';

	type Props = {
		logout(): void;
		isAuthorized?: boolean;
		username?: string;
	};
	let { logout, isAuthorized = $bindable(), username }: Props = $props();
</script>

<nav>
	<div class="nav-center">
		<span>Photobook</span>

		<a href="/">Startseite</a>
		{#if isAuthorized}
			<a href="/user-gallery">Meine Bilder</a>
			<a href="/upload">Hochladen</a>
		{:else}
			<a href="/register">Registrieren</a>
			<a href="/login">Anmelden</a>
		{/if}
	</div>

	<div class="nav-right">
		{#if username}
			<div class="user-info">
				<Fa icon={faUser} />
				<span>{username}</span>
			</div>
		{/if}
		{#if isAuthorized}
			<button id="logoutBtn" onclick={logout}>Ausloggen</button>
		{/if}
	</div>
</nav>

<style>
	nav {
		display: flex;
		align-items: center;
		justify-content: space-between;
		background-color: #f59e0b;
		color: #1e293b;
		padding: 1rem 2rem;
		gap: 2rem;
		font-weight: bold;
	}

	nav span {
		margin: 0;
		flex-shrink: 0;
	}

	.nav-center {
		display: flex;
		gap: 2rem;

		justify-content: center;
	}

	.nav-center a {
		color: #1e293b;
		text-decoration: none;
		transition: opacity 0.2s ease;
	}

	.nav-center a:hover {
		opacity: 0.8;
		text-decoration: underline;
	}

    .user-info {
        display: flex;
        flex-direction: row;
        align-items: center;
        gap: 0.5rem;
    }

	.nav-right {
        display: flex;
        flex-direction: row;
        gap: 1rem;
		flex-shrink: 0;
	}

	#logoutBtn {
		background: none;
		border: none;
		color: #1e293b;
		cursor: pointer;
		font-size: 1rem;
		transition: opacity 0.2s ease;
	}

	#logoutBtn:hover {
		opacity: 0.8;
	}
</style>
