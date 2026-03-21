<script lang="ts">
    type Props = {
        route: string;
        hexStringId: string;
        filename: string;
    }
    
    let { route, hexStringId, filename }: Props = $props();

</script>

<main>
    <div class="photoCard">
        <img src={route} alt={filename} class="photo" />
        <h2>{filename}</h2>
    </div>
</main>