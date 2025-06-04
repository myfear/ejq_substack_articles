document.addEventListener('DOMContentLoaded', () => {
    const postFeed = document.getElementById('post-feed');
    const loadingIndicator = document.getElementById('loading-indicator');
    let eventSource;
    let isFetching = false; // Renamed from isStreaming for clarity with scroll logic
    let postsCurrentlyBeingFetchedCount = 0; // Keep track of how many we expect

    function createPostElement(post) {
        const postDiv = document.createElement('div');
        postDiv.classList.add('post');
        postDiv.setAttribute('data-post-id', post.id);

        const contentP = document.createElement('p');
        contentP.classList.add('post-content');

        if (post.error) {
            postDiv.classList.add('post-error');
            contentP.textContent = `Error: ${post.error}`;
            postDiv.appendChild(contentP);
            return postDiv;
        }

        contentP.textContent = post.text;
        postDiv.appendChild(contentP);

        const actionsDiv = document.createElement('div');
        actionsDiv.classList.add('post-actions');

        const likeButton = document.createElement('button');
        likeButton.innerHTML = '👍 <span class="like-count">Like</span>';
        likeButton.onclick = () => handleLike(post.id, post.text);
        actionsDiv.appendChild(likeButton);

        const dislikeButton = document.createElement('button');
        dislikeButton.innerHTML = '👎 <span class="dislike-count">Dislike</span>';
        dislikeButton.onclick = () => handleDislike(post.id, post.text);
        actionsDiv.appendChild(dislikeButton);

        postDiv.appendChild(actionsDiv);
        return postDiv;
    }

    async function fetchAndDisplayPosts(count = 3) {
        if (isFetching) {
            console.log("Already fetching posts.");
            return;
        }
        isFetching = true;
        loadingIndicator.style.display = 'block';
        postsCurrentlyBeingFetchedCount = count;
        let postsReceivedThisStream = 0;

        let fallbackTimeout = setTimeout(() => {
            console.warn("Fetch timed out, resetting isFetching.");
            isFetching = false;
            loadingIndicator.style.display = 'none';
            if (eventSource) eventSource.close();
        }, 8000); // 8 seconds, adjust as needed

        if (eventSource && eventSource.readyState !== EventSource.CLOSED) {
            eventSource.close();
        }

        eventSource = new EventSource(`/api/posts/stream?count=${count}`);
        console.log(`Requesting ${count} new posts from stream.`);

        eventSource.onmessage = function(event) {
            try {
                const postData = JSON.parse(event.data);
                if (postData.error) {
                    console.error("Error from stream:", postData.error);
                    const errorPostDiv = createPostElement(postData); // Use createPostElement to show error
                    postFeed.appendChild(errorPostDiv);
                } else {
                    const postElement = createPostElement(postData);
                    postFeed.appendChild(postElement); // Append new post
                }
                postsReceivedThisStream++;

                // Check if all requested posts for this stream have been received
                if (postsReceivedThisStream >= postsCurrentlyBeingFetchedCount) {
                    console.log(`Received all ${postsCurrentlyBeingFetchedCount} expected posts for this stream.`);
                    loadingIndicator.style.display = 'none';
                    if(eventSource) eventSource.close(); // Close after receiving expected count
                    isFetching = false; // Reset global fetching flag
                    clearTimeout(fallbackTimeout); // Clear fallback
                    ensureScrollable(); // <-- Call after each fetch
                }

            } catch (e) {
                console.error('Failed to parse post data:', event.data, e);
                // Potentially display a generic error post
            }
        };

        eventSource.onerror = function(err) {
            console.error("EventSource failed:", err);
            loadingIndicator.style.display = 'none';
            if(eventSource) eventSource.close();
            isFetching = false; // Reset global fetching flag on error too
            clearTimeout(fallbackTimeout); // Clear fallback
            // Optionally, try to reconnect or inform the user
        };

        eventSource.onopen = function() {
            console.log("Connection to stream opened.");
        };
    }

    // Initial load
    fetchAndDisplayPosts(3);

    // Ensure enough posts to allow scrolling
    function ensureScrollable() {
        if (document.body.offsetHeight <= window.innerHeight && !isFetching) {
            fetchAndDisplayPosts(2);
            // No need for setTimeout recursion; will be called again after fetch
        }
    }
    setTimeout(ensureScrollable, 700);

    // "Endless" Scrolling Logic
    window.addEventListener('scroll', () => {
        if ((window.innerHeight + window.scrollY) >= document.body.offsetHeight - 200 && !isFetching) {
            console.log("Scrolled to bottom, fetching more posts...");
            fetchAndDisplayPosts(2); // Fetch 2 more on scroll
        }
    });

    // Functions for like/dislike
    window.handleLike = async function(postId, postText) {
        console.log('Liked Post ID:', postId, 'Text:', postText.substring(0,50) + "...");
        try {
            const response = await fetch(`/api/feedback/like`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({text: postText})
            });
            if (response.ok) console.log("Like recorded by backend.");
            else console.error("Failed to record like", await response.text());
        } catch (error) {
            console.error("Error sending like:", error);
        }
    }

    window.handleDislike = async function(postId, postText) {
        console.log('Disliked Post ID:', postId, 'Text:', postText.substring(0,50) + "...");
         try {
            const response = await fetch(`/api/feedback/dislike`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({text: postText})
            });
            if (response.ok) console.log("Dislike recorded by backend.");
            else console.error("Failed to record dislike", await response.text());
        } catch (error) {
            console.error("Error sending dislike:", error);
        }
    }

    console.log("Happy Place UI Initialized and ready for streaming!");
});