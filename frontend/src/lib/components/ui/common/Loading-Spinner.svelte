<script lang="ts">
</script>

<div class="spinner">
	<div class="spinner-circle"></div>
</div>

<style>
	.spinner {
		display: flex;
		justify-content: center;
		align-items: center;
		margin: 2rem 0;
	}

	.spinner-circle {
		border: 4px solid #f3f3f3;
		border-top: 4px solid #F59E0B;
		border-radius: 50%;
		width: 40px;
		height: 40px;
		animation: spin 1s linear infinite;
	}

	@keyframes spin {
		0% {
			transform: rotate(0deg);
		}
		100% {
			transform: rotate(360deg);
		}
	}
</style>
