<script lang="ts">
    const username: string = localStorage.getItem('username') || 'Unknown User';

</script>
<main>
    <div>
        <span>{username}</span>
    </div>
</main>