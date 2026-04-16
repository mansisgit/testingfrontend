document.addEventListener('DOMContentLoaded', () => {
    // Intercept social action forms
    document.body.addEventListener('submit', async (e) => {
        const form = e.target;
        const action = form.getAttribute('action') || '';
        
        const isSocialAction = action.includes('/like') || 
                               action.includes('/comment') || 
                               action.includes('/friendship/') || 
                               action.includes('/notification/delete/');

        if (isSocialAction) {
            e.preventDefault();
            
            const formData = new FormData(form);
            const method = form.getAttribute('method') || 'POST';
            
            // Get token from cookie
            const token = document.cookie.split('; ').find(row => row.startsWith('JWT-TOKEN='))?.split('=')[1];

            try {
                const response = await fetch(action, {
                    method: method,
                    body: formData,
                    headers: {
                        'X-Requested-With': 'XMLHttpRequest',
                        'Authorization': token ? `Bearer ${token}` : ''
                    }
                });

                if (response.ok) {
                    const data = await response.json();
                    
                    if (action.includes('/like')) {
                        handleLikeUpdate(form, data);
                    } else if (action.includes('/comment')) {
                        handleCommentUpdate(form, data);
                    } else if (action.includes('/friendship/request')) {
                        handleFriendRequest(form, data);
                    } else if (action.includes('/friendship/accept') || 
                               action.includes('/friendship/reject') || 
                               action.includes('/notification/delete')) {
                        handleItemRemoval(form, data);
                    }
                }
            } catch (error) {
                console.error('Socials Interaction Error:', error);
            }
        }
    });

    function handleFriendRequest(form, data) {
        const button = form.querySelector('button');
        if (button) {
            button.disabled = true;
            button.textContent = 'Requested';
            button.style.background = '#f1f5f9';
            button.style.color = '#94a3b8';
            button.style.cursor = 'default';
        }
    }

    function handleItemRemoval(form, data) {
        const card = form.closest('.card');
        if (card) {
            card.style.transition = 'all 0.4s cubic-bezier(0.4, 0, 0.2, 1)';
            card.style.opacity = '0';
            card.style.transform = 'translateX(20px)';
            setTimeout(() => {
                card.remove();
                // If list is empty, refresh or show empty state if possible
                // For now, simple removal is best UX
            }, 400);
        }
    }

    function handleLikeUpdate(form, data) {
        const button = form.querySelector('button');
        const countSpan = form.querySelector('.like-count');
        const svg = button.querySelector('svg');

        if (countSpan) countSpan.textContent = data.newLikeCount;

        if (data.isLiked) {
            button.classList.add('text-danger');
            svg.setAttribute('fill', 'currentColor');
        } else {
            button.classList.remove('text-danger');
            svg.setAttribute('fill', 'none');
        }
    }

    function handleCommentUpdate(form, data) {
        const postCard = form.closest('.card');
        const commentsArea = postCard.querySelector('.comments-area');
        const commentCountSpan = postCard.querySelector('.comment-count');

        const commentEl = document.createElement('div');
        commentEl.style.cssText = 'margin-bottom: 1rem; padding-bottom: 1rem; border-bottom: 1px solid #e2e8f0; animation: fadeIn 0.3s ease-out;';
        commentEl.innerHTML = `
            <div class="flex gap-2">
                <span class="username" style="font-size: 0.85rem;">${data.username}</span>
                <span class="timestamp" style="font-size: 0.75rem;">Just now</span>
            </div>
            <p style="font-size: 0.9rem; margin-top: 0.25rem;">${data.content}</p>
        `;

        // Insert before the form
        commentsArea.insertBefore(commentEl, form);

        form.querySelector('input').value = '';
        
        if (commentCountSpan) {
            const currentCount = parseInt(commentCountSpan.textContent) || 0;
            commentCountSpan.textContent = currentCount + 1;
        }
    }
});
