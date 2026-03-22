<script lang="ts">
  type Props = {
    isOpen: boolean;
    imageSrc: string;
    imageAlt?: string;
  }

  let { isOpen = $bindable(false), imageSrc = '', imageAlt = '' }: Props = $props();

  function closeModal(): void {
    isOpen = false;
  }

  function handleBackdropClick(e: MouseEvent): void {
    if (e.target === e.currentTarget) {
      closeModal();
    }
  }

  function handleKeydown(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      closeModal();
    }
  }
</script>

<svelte:window on:keydown={handleKeydown} />

{#if isOpen}

  <div class="modal-backdrop" onclick={handleBackdropClick} role="dialog" aria-modal="true" tabindex="0" onkeydown={handleKeydown}>
    <div class="modal-content">
      <button class="close-button" onclick={closeModal} aria-label="Modal schließen">
        <span>✕</span>
      </button>
      <img src={imageSrc} alt={imageAlt} class="modal-image" />
    </div>
  </div>
{/if}

<style>
  .modal-backdrop {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.8);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
  }

  .modal-content {
    position: relative;
    max-width: 90vw;
    max-height: 90vh;
    background-color: white;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
  }

  .modal-image {
    width: 100%;
    height: 100%;
    object-fit: contain;
    display: block;
  }

  .close-button {
    position: absolute;
    top: 10px;
    right: 10px;
    background-color: rgba(0, 0, 0, 0.5);
    color: white;
    border: none;
    width: 40px;
    height: 40px;
    border-radius: 50%;
    font-size: 24px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background-color 0.2s;
    z-index: 10;
  }

  .close-button:hover {
    background-color: rgba(0, 0, 0, 0.7);
  }
</style>