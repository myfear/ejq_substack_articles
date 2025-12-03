// Carbon Web Components imports
import '@carbon/web-components/es/components/ui-shell/index.js';
import '@carbon/web-components/es/components/button/index.js';
import '@carbon/web-components/es/components/data-table/index.js';
import '@carbon/web-components/es/components/inline-loading/index.js';
import '@carbon/web-components/es/components/heading/index.js';

// Carbon styles
import '@carbon/styles/css/styles.css';

// Optional small layout tweaks
import './app.css';

async function loadProjects() {
    const loading = document.querySelector('#projects-loading');
    const tableBody = document.querySelector('cds-table-body');
    const rowTemplate = document.querySelector('#template--project-row');

    if (!tableBody || !rowTemplate) {
        return;
    }

    try {
        if (loading) {
            loading.status = 'active';
            loading.description = 'Loading projects';
        }

        const response = await fetch('/api/projects');
        if (!response.ok) {
            throw new Error('HTTP ${response.status}');
        }

        const projects = await response.json();

        // Clear existing rows
        tableBody.innerHTML = '';

        projects.forEach((project) => {
            const fragment = rowTemplate.content.cloneNode(true);

            fragment.querySelector('[key ="id"]').textContent = project.id;
            fragment.querySelector('[key ="name"]').textContent = project.name;
            fragment.querySelector('[key ="owner"]').textContent = project.owner;
            fragment.querySelector('[key ="status"]').textContent = project.status;

            tableBody.appendChild(fragment);
        });

        if (loading) {
            loading.status = 'finished';
            loading.description = 'Projects loaded';
        }
    } catch (err) {
        console.error('Failed to load projects', err);
        if (loading) {
            loading.status = 'error';
            loading.description = 'Failed to load projects';
        }
    }
}

window.addEventListener('DOMContentLoaded', () => {
    loadProjects();
});