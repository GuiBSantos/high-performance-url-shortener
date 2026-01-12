import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 100,
    duration: '10s',
    maxRedirects: 0,
};

export default function () {

    const payload = JSON.stringify({
        url: 'https://www.google.com'
    });

    const params = {
        headers: {
            'content-type': 'application/json',
        }
    };

    const createRes = http.post('http://host.docker.internal:8080/api/shorten', payload, params);

    check(createRes, {
        'criou url com sucesso': (r) => r.status === 200 || r.status === 201,
    });

    if (createRes.status !== 200 && createRes.status !== 201) {
        console.error('Falha ao criar URL. Status: ' + createRes.status);
        return;
    }

    const body = JSON.parse(createRes.body);
    const codigoGerado = body.shortCode;

    if (__VU === 1 && __ITER === 0) {
        console.log("Json da API: " + createRes.body);
        console.log("Cod extraido: " + codigoGerado);
        console.log("Url montada: " + `http://host.docker.internal:8080/${codigoGerado}`);
    }

    if (!codigoGerado) {
        console.error("'codigoGerado' está vazio ou undefined!");
        return;
    }

    const accessRes = http.get(`http://host.docker.internal:8080/${codigoGerado}`);

    if (accessRes.status !== 302) {

    }

    check(accessRes, {
        'redirecionou': (r) => r.status === 302,
    });

    sleep(0.1);
}