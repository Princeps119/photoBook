<script lang="ts">
    interface Props {
        currentPage: number;
        totalPages: number;
        onPageChange: (page: number) => void;
    }

    let { currentPage, totalPages, onPageChange } = $props();

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
        background: #0070f3;
        color: #fff;
        border-radius: 5px;
        cursor: pointer;
        transition: background 0.2s;
        font-size: 0.95rem;
    }

    .pagination button:hover:not(:disabled) {
        background: #005bb5;
    }

    .pagination button:disabled {
        background: #ccc;
        cursor: not-allowed;
    }

    .pagination button.active {
        background: #005bb5;
        font-weight: bold;
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