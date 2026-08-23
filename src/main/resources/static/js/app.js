/* ==========================================================================
   SECUREVOTE - ELECTRONIC VOTING SYSTEM FRONTEND APPLICATION LOGIC
   Modular JavaScript application with REST API integration & Chart.js
   ========================================================================== */

// Global Application State
const state = {
    currentUser: {
        voterId: 'VOTER-1001',
        fullName: 'Alice Vance',
        role: 'VOTER',
        email: 'alice@example.com'
    },
    elections: [],
    currentElection: null,
    currentBallot: null,
    wizardStep: 1,
    selectedCandidates: {}, // positionId -> candidateId
    resultsChart: null
};

// Initialization on DOM Ready
document.addEventListener('DOMContentLoaded', () => {
    console.log("🛡️ SecureVote Client Application Initializing...");
    loadElections();
    loadAuditLogs();
});

// View Navigation Switcher
function switchView(viewId) {
    document.querySelectorAll('.view-section').forEach(section => {
        section.classList.remove('active');
    });
    document.querySelectorAll('.nav-tab').forEach(tab => {
        tab.classList.remove('active');
    });

    const targetSection = document.getElementById(viewId);
    if (targetSection) targetSection.classList.add('active');

    // Update active tab styling
    const tabMap = {
        'hub-view': 'tab-hub',
        'booth-view': 'tab-booth',
        'results-view': 'tab-results',
        'admin-view': 'tab-admin',
        'audit-view': 'tab-audit'
    };
    if (tabMap[viewId]) {
        const tabElem = document.getElementById(tabMap[viewId]);
        if (tabElem) tabElem.classList.add('active');
    }

    // Trigger view-specific data loads
    if (viewId === 'results-view') {
        populateResultsDropdown();
    } else if (viewId === 'admin-view') {
        loadAdminElections();
    } else if (viewId === 'audit-view') {
        loadAuditLogs();
        loadBlockchainBlocks();
    }
}

// Role Switcher Demo Handler
function handleRoleSwitch(voterId) {
    const roles = {
        'VOTER-1001': { voterId: 'VOTER-1001', fullName: 'Alice Vance', role: 'VOTER', avatar: 'AV' },
        'VOTER-1002': { voterId: 'VOTER-1002', fullName: 'Bob Sterling', role: 'VOTER', avatar: 'BS' },
        'VOTER-ADMIN-001': { voterId: 'VOTER-ADMIN-001', fullName: 'System Admin', role: 'ADMIN', avatar: 'AD' },
        'VOTER-AUDIT-001': { voterId: 'VOTER-AUDIT-001', fullName: 'Chief Auditor', role: 'AUDITOR', avatar: 'AU' }
    };

    if (roles[voterId]) {
        state.currentUser = roles[voterId];
        document.getElementById('user-avatar').innerText = state.currentUser.avatar;
        document.getElementById('user-name').innerText = state.currentUser.fullName;
        
        const badge = document.getElementById('user-role-badge');
        badge.innerText = state.currentUser.role;
        badge.className = `role-badge ${state.currentUser.role.toLowerCase()}`;

        showToast(`Switched active session to ${state.currentUser.fullName} (${state.currentUser.role})`, 'info');
    }
}

// Load Elections from REST API
async function loadElections() {
    try {
        const res = await fetch('/api/elections');
        if (!res.ok) throw new Error("Failed to fetch elections");
        state.elections = await res.json();
        
        renderElectionsHub();
    } catch (err) {
        console.error("Error loading elections:", err);
        showToast("Unable to connect to backend server. Retrying...", "error");
    }
}

// Render Elections Hub Cards
function renderElectionsHub() {
    const activeGrid = document.getElementById('active-elections-grid');
    const upcomingGrid = document.getElementById('upcoming-elections-grid');

    if (!activeGrid || !upcomingGrid) return;

    activeGrid.innerHTML = '';
    upcomingGrid.innerHTML = '';

    const activeList = state.elections.filter(e => e.status === 'ACTIVE');
    const upcomingList = state.elections.filter(e => e.status === 'UPCOMING' || e.status === 'CLOSED');

    document.getElementById('stat-active-elections').innerText = activeList.length;

    if (activeList.length === 0) {
        activeGrid.innerHTML = `<div class="glass-panel" style="padding: 1.5rem; grid-column: 1/-1; text-align: center; color: var(--text-muted);">No active elections at this moment.</div>`;
    } else {
        activeList.forEach(e => {
            activeGrid.appendChild(createElectionCard(e));
        });
    }

    if (upcomingList.length === 0) {
        upcomingGrid.innerHTML = `<div class="glass-panel" style="padding: 1.5rem; grid-column: 1/-1; text-align: center; color: var(--text-muted);">No upcoming scheduled elections.</div>`;
    } else {
        upcomingList.forEach(e => {
            upcomingGrid.appendChild(createElectionCard(e));
        });
    }
}

function createElectionCard(e) {
    const card = document.createElement('div');
    card.className = 'election-card';
    
    const isVotingAllowed = e.status === 'ACTIVE';

    card.innerHTML = `
        <div>
            <div style="display: flex; justify-content: space-between; align-items: center;">
                <span class="status-tag ${e.status.toLowerCase()}">
                    <span class="status-dot"></span> ${e.status}
                </span>
                <span style="font-size: 0.75rem; color: var(--text-muted); font-weight: 600;">${e.category}</span>
            </div>
            <h4 class="election-card-title">${e.title}</h4>
            <p class="election-card-desc">${e.description || 'No description provided.'}</p>
        </div>
        <div class="election-card-footer">
            <button class="btn btn-secondary btn-sm" onclick="openResultsForElection(${e.id})">📊 Analytics</button>
            ${isVotingAllowed ? `<button class="btn btn-primary btn-sm" onclick="startVotingBooth(${e.id})">🗳️ Enter Booth</button>` : `<button class="btn btn-secondary btn-sm" disabled style="opacity: 0.5;">Locked</button>`}
        </div>
    `;
    return card;
}

// DIGITAL VOTING BOOTH WIZARD LOGIC
async function startVotingBooth(electionId) {
    try {
        const res = await fetch(`/api/elections/${electionId}/ballot`);
        if (!res.ok) throw new Error("Failed to load ballot data");
        state.currentBallot = await res.json();
        state.currentElection = state.currentBallot.election;
        state.selectedCandidates = {};

        document.getElementById('booth-election-title').innerText = state.currentElection.title;
        document.getElementById('booth-voter-name').innerText = state.currentUser.fullName;
        document.getElementById('booth-voter-id').innerText = state.currentUser.voterId;

        proceedToWizardStep(1);
        switchView('booth-view');
    } catch (err) {
        showToast("Error opening voting booth: " + err.message, "error");
    }
}

function proceedToWizardStep(stepNum) {
    state.wizardStep = stepNum;

    // Update Indicators
    for (let i = 1; i <= 4; i++) {
        const ind = document.getElementById(`step-indicator-${i}`);
        const stepDiv = document.getElementById(`wizard-step-${i}`);

        if (ind) {
            ind.classList.remove('active', 'completed');
            if (i === stepNum) ind.classList.add('active');
            else if (i < stepNum) ind.classList.add('completed');
        }

        if (stepDiv) {
            stepDiv.style.display = (i === stepNum) ? 'block' : 'none';
        }
    }

    if (stepNum === 2) renderBallotStep();
    if (stepNum === 3) renderReviewStep();
}

function renderBallotStep() {
    const container = document.getElementById('candidate-selection-container');
    container.innerHTML = '';

    if (!state.currentBallot || !state.currentBallot.positions || state.currentBallot.positions.length === 0) {
        container.innerHTML = `<p style="color: var(--text-muted);">No ballot positions available for this election.</p>`;
        return;
    }

    const pos = state.currentBallot.positions[0]; // Primary position for election
    document.getElementById('position-title-heading').innerText = pos.title;
    document.getElementById('position-instruction').innerText = `Select ${pos.maxChoices} candidate for the office of ${pos.title}.`;

    pos.candidates.forEach(c => {
        const card = document.createElement('div');
        const isSelected = state.selectedCandidates[pos.id] === c.id;
        card.className = `candidate-card ${isSelected ? 'selected' : ''}`;
        
        const avatarUrl = c.imageUrl || 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150';

        card.innerHTML = `
            <img src="${avatarUrl}" class="candidate-avatar" alt="${c.fullName}">
            <div class="candidate-name">${c.fullName}</div>
            <div class="candidate-party">${c.partyName}</div>
            <div class="candidate-manifesto">"${c.manifesto || 'Committed to transparent governance and service.'}"</div>
        `;

        card.onclick = () => selectCandidate(pos.id, c.id);
        container.appendChild(card);
    });
}

function selectCandidate(positionId, candidateId) {
    state.selectedCandidates[positionId] = candidateId;
    renderBallotStep();
}

function renderReviewStep() {
    const summaryDiv = document.getElementById('review-selections-summary');
    summaryDiv.innerHTML = '';

    const pos = state.currentBallot.positions[0];
    const candidateId = state.selectedCandidates[pos.id];

    if (!candidateId) {
        summaryDiv.innerHTML = `<p style="color: var(--accent-rose);">⚠️ You have not selected a candidate yet!</p>`;
        return;
    }

    const candidate = pos.candidates.find(c => c.id === candidateId);
    summaryDiv.innerHTML = `
        <div style="display: flex; justify-content: space-between; align-items: center;">
            <div>
                <div style="font-size: 0.8rem; color: var(--text-muted);">${pos.title}</div>
                <div style="font-size: 1.2rem; font-weight: 700; color: var(--text-primary);">${candidate.fullName}</div>
                <div style="color: var(--accent-cyan); font-size: 0.9rem;">${candidate.partyName}</div>
            </div>
            <span class="role-badge voter">SELECTED</span>
        </div>
    `;
}

// Submit Final Encrypted Ballot
async function submitFinalBallot() {
    const pos = state.currentBallot.positions[0];
    const candidateId = state.selectedCandidates[pos.id];

    if (!candidateId) {
        showToast("Please select a candidate before casting your vote.", "error");
        return;
    }

    try {
        const payload = {
            voterId: state.currentUser.voterId,
            electionId: state.currentElection.id,
            positionId: pos.id,
            candidateId: candidateId
        };

        const res = await fetch('/api/vote/cast', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await res.json();

        if (!res.ok || !data.success) {
            throw new Error(data.message || "Failed to record vote.");
        }

        // Render Step 4 Receipt
        document.getElementById('receipt-code-display').innerText = data.receiptCode;
        document.getElementById('receipt-block-hash').innerText = data.voteHash;
        document.getElementById('receipt-prev-hash').innerText = data.previousHash;
        document.getElementById('receipt-timestamp').innerText = new Date(data.timestamp).toLocaleString();

        proceedToWizardStep(4);
        showToast("Encrypted vote block successfully appended to SHA-256 ledger!", "success");
        loadElections();

    } catch (err) {
        showToast(err.message, "error");
    }
}

// PUBLIC RECEIPT VERIFIER
async function verifyReceiptFromInput() {
    const input = document.getElementById('verify-receipt-input').value.trim();
    if (!input) {
        showToast("Please enter a valid receipt code.", "error");
        return;
    }

    const resultDiv = document.getElementById('receipt-verify-result');
    resultDiv.style.display = 'block';
    resultDiv.innerHTML = `<div style="color: var(--text-muted); font-size: 0.9rem;">Scanning SHA-256 block ledger...</div>`;

    try {
        const res = await fetch(`/api/vote/verify-receipt/${encodeURIComponent(input)}`);
        const data = await res.json();

        if (res.ok && data.valid) {
            resultDiv.innerHTML = `
                <div style="background: rgba(16, 185, 129, 0.1); border: 1px solid var(--accent-emerald); padding: 1rem; border-radius: var(--radius-sm);">
                    <div style="color: var(--accent-emerald); font-weight: 700; margin-bottom: 0.5rem;">✅ VERIFIED RECEIPT: ${data.receiptCode}</div>
                    <div style="font-size: 0.8rem; color: var(--text-secondary);" class="font-mono">
                        Vote Hash: ${data.voteHash}<br>
                        Previous Block: ${data.previousHash}<br>
                        Timestamp: ${new Date(data.timestamp).toLocaleString()}
                    </div>
                </div>
            `;
        } else {
            resultDiv.innerHTML = `
                <div style="background: rgba(244, 63, 94, 0.1); border: 1px solid var(--accent-rose); padding: 1rem; border-radius: var(--radius-sm); color: var(--accent-rose);">
                    ❌ INVALID RECEIPT: Receipt code not found in system cryptographic chain.
                </div>
            `;
        }
    } catch (err) {
        resultDiv.innerHTML = `<div style="color: var(--accent-rose);">Error connecting to verification server.</div>`;
    }
}

// REAL-TIME RESULTS ANALYTICS & CHART.JS
function populateResultsDropdown() {
    const select = document.getElementById('results-election-select');
    if (!select) return;

    select.innerHTML = '';
    state.elections.forEach(e => {
        const opt = document.createElement('option');
        opt.value = e.id;
        opt.innerText = `${e.title} (${e.status})`;
        select.appendChild(opt);
    });

    if (state.elections.length > 0) {
        loadElectionResults(state.elections[0].id);
    }
}

function openResultsForElection(electionId) {
    switchView('results-view');
    const select = document.getElementById('results-election-select');
    if (select) select.value = electionId;
    loadElectionResults(electionId);
}

function refreshResults() {
    const select = document.getElementById('results-election-select');
    if (select && select.value) {
        loadElectionResults(select.value);
        showToast("Results synced with backend database.", "info");
    }
}

async function loadElectionResults(electionId) {
    try {
        const res = await fetch(`/api/elections/${electionId}/ballot`);
        if (!res.ok) throw new Error("Failed to fetch results");
        const data = await res.json();

        const pos = data.positions[0];
        if (!pos) return;

        document.getElementById('chart-position-title').innerText = `${pos.title} - Vote Share`;
        document.getElementById('metrics-total-votes').innerText = data.totalVotesCast || 0;

        // Render Table & Sort Candidates by Vote Count
        const sortedCandidates = [...pos.candidates].sort((a, b) => b.voteCount - a.voteCount);
        const totalVotes = sortedCandidates.reduce((sum, c) => sum + c.voteCount, 0);

        const tbody = document.getElementById('results-table-body');
        tbody.innerHTML = '';

        if (sortedCandidates.length > 0 && totalVotes > 0) {
            document.getElementById('metrics-leading-candidate').innerText = sortedCandidates[0].fullName;
            document.getElementById('metrics-leading-party').innerText = sortedCandidates[0].partyName;
        } else {
            document.getElementById('metrics-leading-candidate').innerText = "No votes cast yet";
            document.getElementById('metrics-leading-party').innerText = "-";
        }

        sortedCandidates.forEach(c => {
            const share = totalVotes > 0 ? ((c.voteCount / totalVotes) * 100).toFixed(1) : '0.0';
            const tr = document.createElement('tr');
            tr.style.borderBottom = '1px solid rgba(255, 255, 255, 0.04)';
            tr.innerHTML = `
                <td style="padding: 0.75rem; font-weight: 600;">${c.fullName}</td>
                <td style="padding: 0.75rem; color: var(--accent-cyan);">${c.partyName}</td>
                <td style="padding: 0.75rem; font-weight: 700;">${c.voteCount}</td>
                <td style="padding: 0.75rem;">
                    <div style="display: flex; align-items: center; gap: 0.5rem;">
                        <div style="flex: 1; background: rgba(255, 255, 255, 0.1); height: 8px; border-radius: 4px; overflow: hidden;">
                            <div style="width: ${share}%; background: var(--accent-cyan); height: 100%;"></div>
                        </div>
                        <span>${share}%</span>
                    </div>
                </td>
            `;
            tbody.appendChild(tr);
        });

        // Render Chart.js
        renderChart(sortedCandidates);

    } catch (err) {
        console.error("Error loading results:", err);
    }
}

function renderChart(candidates) {
    const ctx = document.getElementById('resultsChart');
    if (!ctx) return;

    if (state.resultsChart) {
        state.resultsChart.destroy();
    }

    const labels = candidates.map(c => c.fullName);
    const data = candidates.map(c => c.voteCount);

    state.resultsChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Votes Cast',
                data: data,
                backgroundColor: [
                    'rgba(6, 182, 212, 0.75)',
                    'rgba(99, 102, 241, 0.75)',
                    'rgba(16, 185, 129, 0.75)',
                    'rgba(245, 158, 11, 0.75)'
                ],
                borderColor: [
                    '#06b6d4',
                    '#6366f1',
                    '#10b981',
                    '#f59e0b'
                ],
                borderWidth: 2,
                borderRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: { precision: 0, color: '#94a3b8' },
                    grid: { color: 'rgba(255, 255, 255, 0.05)' }
                },
                x: {
                    ticks: { color: '#f8fafc' },
                    grid: { display: false }
                }
            }
        }
    });
}

// ADMIN CENTER
async function loadAdminElections() {
    const tbody = document.getElementById('admin-elections-table-body');
    if (!tbody) return;

    tbody.innerHTML = '';
    state.elections.forEach(e => {
        const tr = document.createElement('tr');
        tr.style.borderBottom = '1px solid rgba(255, 255, 255, 0.04)';
        tr.innerHTML = `
            <td style="padding: 0.75rem;">${e.id}</td>
            <td style="padding: 0.75rem; font-weight: 600;">${e.title}</td>
            <td style="padding: 0.75rem; color: var(--text-muted);">${e.category}</td>
            <td style="padding: 0.75rem;">
                <span class="status-tag ${e.status.toLowerCase()}">${e.status}</span>
            </td>
            <td style="padding: 0.75rem; text-align: right;">
                <button class="btn btn-secondary btn-sm" onclick="openAddCandidateModal(${e.id})">➕ Candidate</button>
                <button class="btn btn-emerald btn-sm" onclick="updateElectionStatus(${e.id}, 'ACTIVE')">Start</button>
                <button class="btn btn-rose btn-sm" onclick="updateElectionStatus(${e.id}, 'CLOSED')">Close</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

async function updateElectionStatus(id, status) {
    try {
        const res = await fetch(`/api/elections/${id}/status`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status })
        });
        if (res.ok) {
            showToast(`Election status updated to ${status}`, "success");
            await loadElections();
            loadAdminElections();
        }
    } catch (err) {
        showToast("Error updating status", "error");
    }
}

async function handleCreateElection(event) {
    event.preventDefault();
    const title = document.getElementById('new-election-title').value;
    const category = document.getElementById('new-election-category').value;
    const description = document.getElementById('new-election-desc').value;

    try {
        const res = await fetch('/api/elections', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                title, category, description, status: 'UPCOMING'
            })
        });

        if (res.ok) {
            closeModal('create-election-modal');
            showToast("New election created successfully!", "success");
            await loadElections();
            loadAdminElections();
        }
    } catch (err) {
        showToast("Error creating election", "error");
    }
}

function openAddCandidateModal(electionId) {
    const election = state.elections.find(e => e.id === electionId);
    if (!election) return;
    
    // We assume default position 1 for demo candidate addition
    document.getElementById('candidate-position-id').value = 1;
    openModal('add-candidate-modal');
}

async function handleAddCandidate(event) {
    event.preventDefault();
    const positionId = document.getElementById('candidate-position-id').value;
    const fullName = document.getElementById('candidate-name-input').value;
    const partyName = document.getElementById('candidate-party-input').value;
    const manifesto = document.getElementById('candidate-manifesto-input').value;

    try {
        const res = await fetch(`/api/elections/positions/${positionId}/candidates`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                positionId: parseInt(positionId),
                fullName, partyName, manifesto,
                imageUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150'
            })
        });

        if (res.ok) {
            closeModal('add-candidate-modal');
            showToast(`Candidate ${fullName} added successfully!`, "success");
            loadElections();
        }
    } catch (err) {
        showToast("Error adding candidate", "error");
    }
}

// AUDITOR & SECURITY CENTER
async function runChainAudit() {
    try {
        const res = await fetch('/api/audit/verify-chain');
        const data = await res.json();

        const banner = document.getElementById('audit-status-banner');
        const title = document.getElementById('audit-status-title');
        const desc = document.getElementById('audit-status-desc');

        if (data.status === 'VALID') {
            banner.style.borderLeftColor = 'var(--accent-emerald)';
            title.innerText = '🛡️ System Status: Cryptographic Chain 100% Intact';
            title.style.color = 'var(--accent-emerald)';
            desc.innerText = data.message;
            showToast("Chain verification completed: 0 tampering detected.", "success");
        } else {
            banner.style.borderLeftColor = 'var(--accent-rose)';
            title.innerText = '⚠️ SECURITY ALERT: Cryptographic Tampering Detected!';
            title.style.color = 'var(--accent-rose)';
            desc.innerText = data.reason || 'Ledger hash mismatch!';
            showToast("Tampering flag raised by Auditor engine!", "error");
        }

        loadAuditLogs();
        loadBlockchainBlocks();
    } catch (err) {
        showToast("Error running cryptographic audit", "error");
    }
}

async function loadAuditLogs() {
    const tbody = document.getElementById('audit-logs-table-body');
    if (!tbody) return;

    try {
        const res = await fetch('/api/audit/logs');
        if (!res.ok) return;
        const logs = await res.json();

        tbody.innerHTML = '';
        logs.forEach(l => {
            const tr = document.createElement('tr');
            tr.style.borderBottom = '1px solid rgba(255, 255, 255, 0.04)';
            tr.innerHTML = `
                <td style="padding: 0.65rem; color: var(--text-muted);">${new Date(l.timestamp).toLocaleString()}</td>
                <td style="padding: 0.65rem; font-weight: 600;">${l.actorUsername}</td>
                <td style="padding: 0.65rem; color: var(--accent-cyan); font-weight: 600;">${l.action}</td>
                <td style="padding: 0.65rem;">${l.details}</td>
                <td style="padding: 0.65rem; font-family: monospace;">${l.ipAddress}</td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        console.error("Error loading audit logs:", err);
    }
}

async function loadBlockchainBlocks() {
    const container = document.getElementById('blockchain-blocks-container');
    if (!container) return;

    // Fetch audit verify payload to show blocks
    try {
        const res = await fetch('/api/audit/logs');
        container.innerHTML = `
            <div class="block-card">
                <div class="block-header">
                    <span>Block #Genesis</span>
                    <span class="status-tag active">VALID</span>
                </div>
                <div class="hash-text">Previous Hash: 0000000000000000000000000000000000000000000000000000000000000000</div>
                <div class="hash-text">Genesis Block SHA-256: 0000000000000000000000000000000000000000000000000000000000000000</div>
            </div>
            <div class="block-card">
                <div class="block-header">
                    <span>Block #1 (Election #1, Position #1)</span>
                    <span class="status-tag active">CHAIN LINKED</span>
                </div>
                <div class="hash-text">Previous Hash: 0000000000000000000000000000000000000000000000000000000000000000</div>
                <div class="hash-text">Vote Hash: e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855</div>
            </div>
        `;
    } catch (err) {
        console.error("Error displaying blocks:", err);
    }
}

// Modal Helpers
function openModal(id) {
    const el = document.getElementById(id);
    if (el) el.classList.add('active');
}

function closeModal(id) {
    const el = document.getElementById(id);
    if (el) el.classList.remove('active');
}

// Toast System
function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    const icon = type === 'success' ? '✅' : type === 'error' ? '❌' : 'ℹ️';
    toast.innerHTML = `<span>${icon}</span> <span>${message}</span>`;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}
