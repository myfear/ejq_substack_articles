// A global variable to hold the current mission data
let currentMission = null;

// Function to request a new mission from the backend
async function getNewMission() {
    const missionDetailsDiv = document.getElementById('mission-details');
    missionDetailsDiv.innerHTML = "Generating new mission briefing... Stand by...";
    document.getElementById('accept-mission-button').style.display = 'none';

    try {
        const response = await fetch('/mission/new');
        if (!response.ok) {
            missionDetailsDiv.innerHTML = "Error: Could not retrieve mission from HQ.";
            return;
        }
        currentMission = await response.json();
        renderMission(currentMission);
    } catch (error) {
        console.error("Failed to fetch mission:", error);
        missionDetailsDiv.innerHTML = "Communication with HQ failed. Check console.";
    }
}

// Function to render the mission, highlighting the PII
function renderMission(mission) {
    let highlightedText = mission.originalText;
    
    // Sort entities by end position in reverse order to avoid index shifting issues
    const sortedEntities = mission.entities.sort((a, b) => b.end - a.end);

    sortedEntities.forEach(entity => {
        const piiText = highlightedText.substring(entity.start, entity.end);
        const highlightedPii = `<span class="pii-highlight" title="${entity.entityType}">${piiText}</span>`;
        highlightedText = highlightedText.substring(0, entity.start) + highlightedPii + highlightedText.substring(entity.end);
    });

    document.getElementById('mission-details').innerHTML = highlightedText;
    document.getElementById('accept-mission-button').style.display = 'block';
}

// Function to accept the mission and redact the PII
function acceptMission() {
    if (currentMission) {
        // The confidential information "dissolves"
        document.getElementById('mission-details').innerHTML = currentMission.redactedText;
        document.getElementById('accept-mission-button').style.display = 'none';
    }
}

// Add event listeners to buttons
window.onload = () => {
    document.getElementById('new-mission-button').addEventListener('click', getNewMission);
    document.getElementById('accept-mission-button').addEventListener('click', acceptMission);
    
    // Request the first mission automatically
    getNewMission();
};