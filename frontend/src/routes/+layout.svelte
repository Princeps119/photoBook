<script lang="ts">
	import './layout.css';
	import favicon from '$lib/assets/favicon.svg';
	import { page } from '$app/state';
	import Header from '$lib/components/ui/common/Nav-Header.svelte';

	let { children, data } = $props();
	let pagename = page.url.pathname;
	let isLoggedIn = $derived(data.isAuthenticated || false);
	let username = $derived(data.username || '');

	let title = $derived('');
	if (pagename === '/') {
		title = 'Galery';
	} else {
		title = pagename.slice(1).charAt(0).toUpperCase() + pagename.slice(2);
	}
	console.log(title)

	function logout() {
		fetch('/api/auth/logout', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			}
		})
		.then(response => response.json())
		.then(data => {
			if (data.success) {
				isLoggedIn = false;
				window.location.href = '/';
			} else {
				console.error('Logout failed:', data.message);
			}
		})
		.catch(error => {
			console.error('Error during logout:', error);
		});
	}
</script>

<svelte:head><link rel="icon" href={favicon}/><title>{title}</title></svelte:head>

<Header logout={logout} bind:isAuthorized={isLoggedIn} username={username} />


{@render children()}
