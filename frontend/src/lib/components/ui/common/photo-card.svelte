<script lang="ts">
  type Props = {
    src: string;
    alt?: string;
    title?: string;
    description?: string;

  }

  let { src, alt = 'Photo', title = 'Photo Title', description = 'Photo description goes here.' }: Props = $props();
</script>


    <div class="mainCard">
        <img {src} {alt} class="photo" />
        <div class="photoInfo">
            <h2>{title}</h2>
            <p>Typ:{description}</p>
        </div>
    </div>


<style>
    .mainCard {
        display: flex;
        flex-direction: column;
        align-items: center;
        background-color: #ffffff38;
        width: 250px;
        height: 350px;
        border-radius: 8px;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        padding: 16px;
        margin: 16px auto;
        flex-shrink: 0;
    }

    .photo {
        width: 250px;
        height: 250px;
        object-fit: cover;
        border-radius: 4px;
        flex-shrink: 0;
    }

    .photoInfo {
        text-align: center;
        margin-top: 12px;
        width: 100%;
    }

    .photoInfo h2 {
        font-size: 1.25rem;
        margin-bottom: 8px;
        margin: 0;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .photoInfo p {
        font-size: 1rem;
        color: #666;
    }
</style>