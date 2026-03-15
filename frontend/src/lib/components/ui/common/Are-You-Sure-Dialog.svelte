<script lang="ts">
  interface Props {
    isOpen: boolean;
    title?: string;
    message: string;
    confirmText?: string;
    cancelText?: string;
    onConfirm?: () => void;
    onCancel?: () => void;
  }

  let {
    isOpen = $bindable(false),
    title = 'Bestätigung erforderlich',
    message = '',
    confirmText = 'Bestätigen',
    cancelText = 'Abbrechen',
    onConfirm,
    onCancel
  }: Props = $props();

  function handleConfirm(): void {
    onConfirm?.();
    isOpen = false;
  }

  function handleCancel(): void {
    onCancel?.();
    isOpen = false;
  }

  function handleKeydown(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      handleCancel();
    }
    if (event.key === 'Enter') {
      handleConfirm();
    }
  }
</script>

<svelte:window on:keydown={handleKeydown} />

{#if isOpen}
  <!-- svelte-ignore a11y_click_events_have_key_events -->

  <div class="dialog-backdrop" role="presentation" onclick={(e) => e.target === e.currentTarget && handleCancel()}>
    <div class="dialog-content" role="alertdialog" aria-modal="true" aria-labelledby="dialog-title">
      <h2 id="dialog-title" class="dialog-title">{title}</h2>
      
      <p class="dialog-message">{message}</p>

      <div class="dialog-actions">
        <button class="btn-secondary" onclick={handleCancel}>
          {cancelText}
        </button>
        <button 
          class="btn-danger"
          onclick={handleConfirm}
        >
          {confirmText}
        </button>
      </div>
    </div>
  </div>
{/if}

<style>
  .dialog-backdrop {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
  }

  .dialog-content {
    position: relative;
    background-color: #1E293B;
    border-radius: 8px;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
    padding: 32px;
    max-width: 400px;
    min-width: 300px;
    animation: slideIn 0.3s ease-out;
  }

  @keyframes slideIn {
    from {
      opacity: 0;
      transform: translateY(-20px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  .dialog-title {
    font-size: 20px;
    font-weight: 600;
    margin: 0 0 12px 0;
    color: white;
  }

  .dialog-message {
    font-size: 14px;
    color: white;
    margin: 0 0 24px 0;
    line-height: 1.5;
  }

  .dialog-actions {
    display: flex;
    gap: 12px;
    justify-content: flex-end;
  }

  .btn-danger {
    padding: 8px 16px;
    border: none;
    border-radius: 6px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
    background-color: #F59E0B;
    color: white;
  }

  .btn-danger:hover {
    background-color: #f49b00;
  }

  .btn-secondary {
    padding: 8px 16px;
    border: none;
    border-radius: 6px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
    background-color: #e5e7eb;
    color: #1f2937;
  }

  .btn-secondary:hover {
    background-color: #d1d5db;
  }
</style>