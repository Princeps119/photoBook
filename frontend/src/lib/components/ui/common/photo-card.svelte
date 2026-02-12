<script lang="ts">
  type Props = {
    src: string;
    alt?: string;
    title?: string;
    description?: string;
  }

  let { src, alt = 'Photo', title = 'Photo Title', description = 'Photo description goes here.' }: Props = $props();
</script>

<main>
    <div class="mainCard">
        <img {src} {alt} class="photo" />
        <div class="photoInfo">
            <h2>{title}</h2>
            <p>{description}</p>
        </div>
    </div>
</main>