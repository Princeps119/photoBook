<script lang="ts">
    interface Props {
        currentPage: number;
        totalPages: number;
        onPageChange: (page: number) => void;
    }

    let { currentPage, totalPages, onPageChange }: Props = $props();

    function goToPage(page: number) {
        if (page >= 1 && page <= totalPages) {
            onPageChange(page);
        }
    }
</script>

<style>
    .pagination {
        display: flex;
        justify-content: center;
        align-items: center;
        gap: 0.5rem;
        flex-wrap: wrap;
    }

    .pagination button {
        padding: 0.5rem 1rem;
        border: none;
        background: #F59E0B;
        color: #1E293B;
        border-radius: 5px;
        cursor: pointer;
        transition: background 0.2s;
        font-size: 0.95rem;
    }

    .pagination button:hover:not(:disabled) {
        background: #F59E0B;
    }

    .pagination button:disabled {
        background: #3c485b;
        cursor: not-allowed;
    }

    .pagination button.active {
        background: #F59E0B;
        font-weight: bold;
        scale: 1.1;
    }
</style>

{#if totalPages > 1}
    <div class="pagination">
        <button onclick={() => goToPage(currentPage - 1)} disabled={currentPage === 1}>
            ← Prev
        </button>

        {#each Array.from({ length: totalPages }, (_, i) => i + 1) as page}
            <button class:active={currentPage === page} onclick={() => goToPage(page)}>
                {page}
            </button>
        {/each}

        <button onclick={() => goToPage(currentPage + 1)} disabled={currentPage === totalPages}>
            Next →
        </button>
    </div>
{/if}