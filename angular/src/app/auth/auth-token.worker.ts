declare const self: SharedWorkerGlobalScope;

interface TokenState {
    token: string | null;
    roles: string | null;
}

const state: TokenState = {token: null, roles: null};
const ports: MessagePort[] = [];

function broadcast(data: object): void {
    ports.forEach(port => port.postMessage(data));
}

self.onconnect = (event: MessageEvent) => {
    const port = event.ports[0];
    ports.push(port);

    port.onmessage = ({data}: MessageEvent) => {
        switch (data.type) {
            case 'GET_STATE':
                port.postMessage({type: 'STATE', token: state.token, roles: state.roles});
                break;
            case 'SET_TOKEN':
                state.token = data.value;
                broadcast({type: 'STATE', token: state.token, roles: state.roles});
                break;
            case 'SET_ROLES':
                state.roles = data.value;
                broadcast({type: 'STATE', token: state.token, roles: state.roles});
                break;
            case 'CLEAR':
                state.token = null;
                state.roles = null;
                broadcast({type: 'STATE', token: state.token, roles: state.roles});
                break;
        }
    };

    port.start();
};
