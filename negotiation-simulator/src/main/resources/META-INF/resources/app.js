let sessionId = null;

// Helper function to get personality display names and descriptions
function getPersonalityInfo(personality) {
    const info = {
        'SUPPORTIVE': {
            title: 'Supportive Manager',
            description: 'Encouraging, collaborative, and helpful approach to discussions'
        },
        'ANALYTICAL': {
            title: 'Analytical Manager',
            description: 'Data-driven, seeks evidence and logical arguments'
        },
        'DIRECT': {
            title: 'Direct Manager',
            description: 'Blunt, time-conscious, and straight to the point'
        },
        'ACCOMMODATING': {
            title: 'Accommodating Manager',
            description: 'Agreeable but cautious, often needs time to consider decisions'
        }
    };
    return info[personality] || { title: personality, description: '' };
}

// Helper function to get scenario display names and descriptions
function getScenarioInfo(scenario) {
    const info = {
        'REQUESTING_RAISE': {
            title: 'Salary Increase Request',
            description: 'You are requesting a salary increase based on your performance'
        },
        'POOR_PERFORMANCE': {
            title: 'Performance Discussion',
            description: 'Manager is addressing performance concerns with you'
        }
    };
    return info[scenario] || { title: scenario, description: '' };
}

async function fetchOptions() {
    try {
        const personalities = await fetch('/api/personalities').then(r => r.json());
        const scenarios = await fetch('/api/scenarios').then(r => r.json());

        // Render personality options as cards
        document.getElementById('personalities').innerHTML = personalities.map(p => {
            const info = getPersonalityInfo(p);
            return `
                <div class="option-card" onclick="selectOption('personality', '${p}', this)">
                    <input type="radio" name="personality" value="${p}">
                    <div class="option-title">${info.title}</div>
                    <div class="option-description">${info.description}</div>
                </div>
            `;
        }).join('');

        // Render scenario options as cards
        document.getElementById('scenarios').innerHTML = scenarios.map(s => {
            const info = getScenarioInfo(s);
            return `
                <div class="option-card" onclick="selectOption('scenario', '${s}', this)">
                    <input type="radio" name="scenario" value="${s}">
                    <div class="option-title">${info.title}</div>
                    <div class="option-description">${info.description}</div>
                </div>
            `;
        }).join('');
    } catch (error) {
        console.error('Error fetching options:', error);
        showError('Failed to load options. Please refresh the page.');
    }
}

// Handle option selection with visual feedback
function selectOption(type, value, element) {
    // Remove selected class from all options of this type
    const container = element.closest('.selection-options');
    container.querySelectorAll('.option-card').forEach(card => {
        card.classList.remove('selected');
    });

    // Add selected class to clicked option
    element.classList.add('selected');

    // Set the radio button value
    const radio = element.querySelector('input[type="radio"]');
    radio.checked = true;

    // Enable start button if both options are selected
    updateStartButton();
}

function updateStartButton() {
    const personality = document.querySelector('input[name="personality"]:checked');
    const scenario = document.querySelector('input[name="scenario"]:checked');
    const userId = document.getElementById('userId').value.trim();

    const startButton = document.querySelector('.start-button');
    startButton.disabled = !(personality && scenario && userId);
}

// Add event listener for name input
document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('userId').addEventListener('input', updateStartButton);
    fetchOptions();
});

document.getElementById('setupForm').addEventListener('submit', async e => {
    e.preventDefault();

    const userId = document.getElementById('userId').value.trim();
    const personalityElement = document.querySelector('input[name="personality"]:checked');
    const scenarioElement = document.querySelector('input[name="scenario"]:checked');

    if (!personalityElement || !scenarioElement || !userId) {
        showError('Please select both a personality and scenario, and enter your name.');
        return;
    }

    const personality = personalityElement.value;
    const scenario = scenarioElement.value;

    try {
        showLoading(true);
        const res = await fetch('/api/sessions', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userId, personality, scenario })
        });

        if (!res.ok) {
            throw new Error(`HTTP ${res.status}: ${res.statusText}`);
        }

        const data = await res.json();
        sessionId = data.id;

        // Hide setup and show chat
        document.getElementById('setup').classList.add('hidden');
        document.getElementById('chat').classList.remove('hidden');

        // Clear messages
        document.getElementById('messages').innerHTML = '';

        showLoading(false);
    } catch (error) {
        console.error('Error starting session:', error);
        showError('Failed to start session. Please try again.');
        showLoading(false);
    }
});

document.getElementById('messageForm').addEventListener('submit', async e => {
    e.preventDefault();

    const messageInput = document.getElementById('messageInput');
    const content = messageInput.value.trim();

    if (!content) return;

    try {
        // Disable input while sending
        messageInput.disabled = true;

        // Add user message to chat
        addMessage('user', content);

        const res = await fetch(`/api/sessions/${sessionId}/messages`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ content })
        });

        if (!res.ok) {
            throw new Error(`HTTP ${res.status}: ${res.statusText}`);
        }

        const msg = await res.json();

        // Add AI response to chat
        addMessage('ai', msg.content);

        messageInput.value = '';
    } catch (error) {
        console.error('Error sending message:', error);
        showError('Failed to send message. Please try again.');
    } finally {
        messageInput.disabled = false;
        messageInput.focus();
    }
});

function addMessage(sender, content) {
    const messagesDiv = document.getElementById('messages');
    const messageElement = document.createElement('div');
    messageElement.className = `message ${sender}`;

    const senderLabel = sender === 'user' ? 'You' : 'Manager';
    messageElement.innerHTML = `<strong>${senderLabel}:</strong> ${content}`;

    messagesDiv.appendChild(messageElement);
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
}

document.getElementById('endSessionBtn').addEventListener('click', async () => {
    try {
        showLoading(true);

        const feedback = await fetch(`/api/sessions/${sessionId}/feedback`).then(r => {
            if (!r.ok) {
                throw new Error(`HTTP ${r.status}: ${r.statusText}`);
            }
            return r.json();
        });

        // Hide chat and show feedback
        document.getElementById('chat').classList.add('hidden');
        document.getElementById('feedback').classList.remove('hidden');

        // Display score with animation
        const score = feedback.overallScore || 0;
        document.getElementById('score').textContent = score;

        // Update the circular progress
        const scoreCircle = document.querySelector('.score-circle');
        scoreCircle.style.setProperty('--score', score);

        // Display strengths
        const strengthsList = document.getElementById('strengths');
        strengthsList.innerHTML = (feedback.strengths || []).map(s => `<li>${s}</li>`).join('');

        // Display improvements
        const improvementsList = document.getElementById('improvements');
        improvementsList.innerHTML = (feedback.improvements || []).map(i => `<li>${i}</li>`).join('');

        showLoading(false);
    } catch (error) {
        console.error('Error getting feedback:', error);
        showError('Failed to get feedback. Please try again.');
        showLoading(false);
    }
});

function showError(message) {
    // Simple error display - you could enhance this with a proper modal
    alert(`Error: ${message}`);
}

function showLoading(show) {
    // Simple loading state - you could enhance this with a proper spinner
    const buttons = document.querySelectorAll('button');
    buttons.forEach(button => {
        button.disabled = show;
        if (show) {
            button.style.opacity = '0.6';
            button.style.cursor = 'not-allowed';
        } else {
            button.style.opacity = '';
            button.style.cursor = '';
        }
    });
}