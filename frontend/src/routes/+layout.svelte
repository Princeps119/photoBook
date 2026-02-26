<script lang="ts">
	import './layout.css';
	import favicon from '$lib/assets/favicon.svg';
	import { page } from '$app/state';
	import Header from '$lib/components/ui/common/header.svelte';

	let { children } = $props();
	let pagename = page.url.pathname;
	let isLoggedIn = true; 
	let title = $state('');
	if (pagename === '/') {
		title = 'Galery';
	} else {
		title = pagename.slice(1).charAt(0).toUpperCase() + pagename.slice(2);
	}
	console.log(title)
</script>

<svelte:head><link rel="icon" href={favicon}/><title>{title}</title></svelte:head>
{#if isLoggedIn}
<Header/>
{/if}

{@render children()}
