<script lang="ts">
    import { api } from "../../apis/api";

    interface UploadedFile {
        file: File;
        title: string;
        description: string;
        type: 'public' | 'private';
    }

    let uploadedFiles: UploadedFile[] = [];
    let isUploading = false;
    let message = '';
    let isDragOver = false;
    let userEmail = '';
    let token = '';
    let photoType: 'public' | 'private' = 'public';

    const handleFileSelect = (files: FileList | null) => {
        if (!files) return;

        const newFiles = Array.from(files).map((file) => ({
            file,
            title: file.name.replace(/\.[^/.]+$/, ''),
            description: '',
            type: photoType
        }));

        uploadedFiles = [...uploadedFiles, ...newFiles];
    };

    const removeFile = (index: number) => {
        uploadedFiles = uploadedFiles.filter((_, i) => i !== index);
    };

    const handleClick = () => {
        document.getElementById('fileInput')?.click();
    };

    const handleKeyDown = (e: KeyboardEvent) => {
        if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            handleClick();
        }
    };

    const handleDrop = (e: DragEvent) => {
        e.preventDefault();
        e.stopPropagation();
        isDragOver = false;
        handleFileSelect(e.dataTransfer?.files ?? null);
    };

    const handleDragOver = (e: DragEvent) => {
        e.preventDefault();
        e.stopPropagation();
        isDragOver = true;
    };

    const handleDragLeave = (e: DragEvent) => {
        e.preventDefault();
        e.stopPropagation();
        isDragOver = false;
    };

    const handleFileChange = (e: Event) => {
        const input = e.target as HTMLInputElement;
        handleFileSelect(input.files);
    };

    const handleUpload = async () => {
        if (uploadedFiles.length === 0) {
            message = 'Bitte wähle mindestens eine Datei aus.';
            return;
        }
       
        isUploading = true;
        message = '';
        let email = "MaxMustermann@muster.de"

        try {
            for (const { file, title, description, type } of uploadedFiles) {
                await api.Photos.post(file, title, description, type, email);
            }

            message = `${uploadedFiles.length} Foto(s) erfolgreich hochgeladen!`;
            uploadedFiles = [];
            const fileInput = document.getElementById('fileInput') as HTMLInputElement;
            if (fileInput) fileInput.value = '';
        } catch (error) {
            message = `Fehler beim Hochladen: ${error instanceof Error ? error.message : 'Unbekannter Fehler'}`;
        } finally {
            isUploading = false;
        }
    };
</script>

<main>
    {#if uploadedFiles.length === 0}
        <div 
            class="upload-container"
            class:drag-over={isDragOver}
            ondrop={handleDrop}
            ondragover={handleDragOver}
            ondragleave={handleDragLeave}
            onclick={handleClick}
            onkeydown={handleKeyDown}
            role="button"
            tabindex="0"
        >
            <div class="upload-content">
                <svg class="upload-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                    <polyline points="17 8 12 3 7 8"></polyline>
                    <line x1="12" y1="3" x2="12" y2="15"></line>
                </svg>
                <h2>Fotos hier ablegen</h2>
                <p class="subtitle">oder zum Durchsuchen klicken</p>
            </div>
            <input 
                type="file" 
                id="fileInput" 
                multiple 
                accept="image/*"
                onchange={handleFileChange}
                hidden
            />
        </div>
    {:else}
        <div class="files-container">
            <div class="files-list">
                {#each uploadedFiles as file, index (index)}
                    <div class="file-item">
                        <img 
                            src={URL.createObjectURL(file.file)} 
                            alt="Vorschau"
                            class="file-preview"
                        />
                        <div class="file-info">
                            <input 
                                type="text" 
                                placeholder="Titel"
                                bind:value={file.title}
                                disabled={isUploading}
                                class="file-input"
                            />
                            <textarea 
                                placeholder="Beschreibung (optional)"
                                bind:value={file.description}
                                disabled={isUploading}
                                class="file-textarea"
                                rows="2"
                            ></textarea>
                            <select
                                bind:value={file.type}
                                disabled={isUploading}
                                class="file-input"
                            >
                                <option selected value="Public">Öffentlich</option>
                                <option value="Private">Privat</option>
                            </select> 
                        </div>
                        <button 
                            onclick={() => removeFile(index)}
                            disabled={isUploading}
                            class="remove-btn"
                            title="Entfernen"
                        >
                            ✕
                        </button>
                    </div>
                {/each}
            </div>

            <div class="actions">
                <button 
                    onclick={handleClick}
                    disabled={isUploading}
                    class="add-more-btn"
                >
                    + Weitere Fotos hinzufügen
                </button>
                <button 
                    onclick={handleUpload}
                    disabled={isUploading}
                    class="upload-btn"
                >
                    {isUploading ? 'Wird hochgeladen...' : 'Hochladen'}
                </button>
            </div>
        </div>
    {/if}

    {#if message}
        <p class="message" class:success={message.includes('erfolgreich')}>{message}</p>
    {/if}
</main>

<style>
    main {
        max-width: 900px;
        margin: 0 auto;
        padding: 40px 20px;
    }

    h1 {
        font-size: 2rem;
        margin-bottom: 30px;
        color: white;
        text-align: center;
    }

    h2 {
        font-size: 1.3rem;
        margin-bottom: 20px;
        color: #333;
    }

    /* Upload Container */
    .upload-container {
        border: 3px dashed #3b82f6;
        border-radius: 12px;
        padding: 60px 20px;
        background-color: #f0f9ff;
        cursor: pointer;
        transition: all 0.3s ease;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
    }

    .upload-container:hover {
        border-color: #2563eb;
        background-color: #e0f2fe;
    }

    .upload-container.drag-over {
        border-color: #1d4ed8;
        background-color: #bfdbfe;
        transform: scale(1.02);
    }

    .upload-content {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 15px;
        pointer-events: none;
    }

    .upload-icon {
        width: 60px;
        height: 60px;
        color: #3b82f6;
    }

    .upload-container h2 {
        margin: 0;
        font-size: 1.3rem;
        color: #1f2937;
    }

    .subtitle {
        margin: 0;
        color: #6b7280;
        font-size: 0.95rem;
    }

    /* Files Container */
    .files-container {
        display: flex;
        flex-direction: column;
        gap: 20px;
    }

    .files-list {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
        gap: 20px;
    }

    .file-item {
        background: white;
        border-radius: 12px;
        overflow: hidden;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        transition: transform 0.3s ease, box-shadow 0.3s ease;
        position: relative;
        display: flex;
        flex-direction: column;
    }

    .file-item:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
    }

    .file-preview {
        width: 100%;
        height: 180px;
        object-fit: cover;
        background-color: #f3f4f6;
    }

    .file-info {
        padding: 15px;
        display: flex;
        flex-direction: column;
        gap: 10px;
        flex: 1;
    }

    .file-input,
    .file-textarea {
        width: 100%;
        padding: 8px 10px;
        border: 1px solid #e5e7eb;
        border-radius: 6px;
        font-family: inherit;
        font-size: 0.95rem;
        transition: border-color 0.2s ease;
    }

    .file-input:focus,
    .file-textarea:focus {
        outline: none;
        border-color: #3b82f6;
        box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
    }

    .file-input:disabled,
    .file-textarea:disabled {
        background-color: #f9fafb;
        color: #9ca3af;
        cursor: not-allowed;
    }

    .file-textarea {
        resize: vertical;
        min-height: 50px;
    }

    .remove-btn {
        position: absolute;
        top: 10px;
        right: 10px;
        width: 30px;
        height: 30px;
        padding: 0;
        border: none;
        border-radius: 6px;
        background-color: rgba(239, 68, 68, 0.9);
        color: white;
        font-size: 1.2rem;
        cursor: pointer;
        transition: background-color 0.2s ease;
    }

    .remove-btn:hover:not(:disabled) {
        background-color: #dc2626;
    }

    .remove-btn:disabled {
        opacity: 0.5;
        cursor: not-allowed;
    }

    /* Actions */
    .actions {
        display: flex;
        gap: 10px;
        justify-content: center;
    }

    .add-more-btn,
    .upload-btn {
        padding: 12px 24px;
        border: none;
        border-radius: 8px;
        font-size: 1rem;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.3s ease;
    }

    .add-more-btn {
        background-color: #f0f9ff;
        color: #3b82f6;
        border: 2px solid #3b82f6;
    }

    .add-more-btn:hover:not(:disabled) {
        background-color: #e0f2fe;
    }

    .upload-btn {
        background-color: #3b82f6;
        color: white;
        flex: 1;
    }

    .upload-btn:hover:not(:disabled) {
        background-color: #2563eb;
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
    }

    .upload-btn:disabled,
    .add-more-btn:disabled {
        opacity: 0.6;
        cursor: not-allowed;
    }

    /* Message */
    .message {
        margin-top: 20px;
        padding: 15px 20px;
        border-radius: 8px;
        font-weight: 500;
        animation: slideIn 0.3s ease;
        text-align: center;
    }

    .message.success {
        background-color: #e8f5e9;
        color: #2e7d32;
        border-left: 4px solid #4caf50;
    }

    .message:not(.success) {
        background-color: #ffebee;
        color: #c62828;
        border-left: 4px solid #f44336;
    }

    @keyframes slideIn {
        from {
            opacity: 0;
            transform: translateY(-10px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }

    @media (max-width: 768px) {
        .files-list {
            grid-template-columns: 1fr;
        }

        .actions {
            flex-direction: column;
        }

        .upload-btn {
            flex: 1;
        }
    }
</style>