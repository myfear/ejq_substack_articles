const colors = ['#3366cc', '#dc3912', '#ff9900', '#109618', '#990099', '#0099c6'];
let clusters = [];

async function loadData() {
  try {
    const response = await fetch('/clusters');
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }
    clusters = await response.json();
    renderVisualization();
    renderClusters();
    updateStats();
    hideLoading();
  } catch (error) {
    console.error('Error loading data:', error);
    showError();
  }
}

function hideLoading() {
  const loadingElement = document.getElementById('loading');
  const contentElement = document.getElementById('content');
  if (loadingElement) loadingElement.style.display = 'none';
  if (contentElement) contentElement.style.display = 'block';
}

function showError() {
  const loadingElement = document.getElementById('loading');
  const errorElement = document.getElementById('error');
  if (loadingElement) loadingElement.style.display = 'none';
  if (errorElement) errorElement.style.display = 'block';
}

function updateStats() {
  const totalArticles = clusters.reduce((sum, cluster) => sum + cluster.articles.length, 0);
  const totalKeywords = clusters.reduce((sum, cluster) => sum + cluster.keywords.length, 0);
  
  const clusterCountElement = document.getElementById('cluster-count');
  const articleCountElement = document.getElementById('article-count');
  const keywordCountElement = document.getElementById('keyword-count');
  
  if (clusterCountElement) clusterCountElement.textContent = clusters.length;
  if (articleCountElement) articleCountElement.textContent = totalArticles;
  if (keywordCountElement) keywordCountElement.textContent = totalKeywords;
}

function renderVisualization() {
  const canvas = document.getElementById('plot');
  if (!canvas) return;
  
  const ctx = canvas.getContext('2d');
  
  // Clear canvas
  ctx.clearRect(0, 0, canvas.width, canvas.height);
  
  if (clusters.length === 0) return;

  // Collect all coordinates
  let allCoords = [];
  clusters.forEach(cluster => {
    cluster.articles.forEach(article => {
      allCoords.push({ x: article.x, y: article.y });
    });
  });

  if (allCoords.length === 0) return;

  // Calculate bounds
  const xs = allCoords.map(c => c.x);
  const ys = allCoords.map(c => c.y);
  const minX = Math.min(...xs);
  const maxX = Math.max(...xs);
  const minY = Math.min(...ys);
  const maxY = Math.max(...ys);

  // Add padding
  const padding = 20;
  const rangeX = maxX - minX;
  const rangeY = maxY - minY;
  const scaleFactorX = (canvas.width - 2 * padding) / rangeX;
  const scaleFactorY = (canvas.height - 2 * padding) / rangeY;

  function scaleX(x) { return padding + (x - minX) * scaleFactorX; }
  function scaleY(y) { return padding + (y - minY) * scaleFactorY; }

  // Draw points for each cluster
  clusters.forEach((cluster, clusterIndex) => {
    ctx.fillStyle = colors[clusterIndex % colors.length];
    ctx.strokeStyle = colors[clusterIndex % colors.length];
    
    cluster.articles.forEach(article => {
      const x = scaleX(article.x);
      const y = scaleY(article.y);
      
      // Draw circle
      ctx.beginPath();
      ctx.arc(x, y, 4, 0, 2 * Math.PI);
      ctx.fill();
      
      // Draw border
      ctx.beginPath();
      ctx.arc(x, y, 4, 0, 2 * Math.PI);
      ctx.stroke();
    });
  });

  // Draw legend
  ctx.font = '12px sans-serif';
  clusters.forEach((cluster, clusterIndex) => {
    const x = 20;
    const y = 20 + clusterIndex * 20;
    
    ctx.fillStyle = colors[clusterIndex % colors.length];
    ctx.fillRect(x, y - 8, 12, 12);
    
    ctx.fillStyle = '#333';
    ctx.fillText(`Cluster ${cluster.clusterId} (${cluster.articles.length} articles)`, x + 20, y);
  });
}

function renderClusters() {
  const container = document.getElementById('clusters');
  if (!container) return;
  
  container.innerHTML = '';

  clusters.forEach((cluster, index) => {
    const clusterDiv = document.createElement('div');
    clusterDiv.className = 'cluster';
    
    clusterDiv.innerHTML = `
      <div class="cluster-header">
        <div class="cluster-id">Cluster ${cluster.clusterId}</div>
        <div class="keywords">${cluster.keywords.join(', ')}</div>
      </div>
      <div class="articles">
        ${cluster.articles.map(article => `
          <div class="article">
            <a href="${article.url}" target="_blank">${article.url}</a>
          </div>
        `).join('')}
      </div>
    `;
    
    container.appendChild(clusterDiv);
  });
}

// Load data when page loads
document.addEventListener('DOMContentLoaded', loadData);
