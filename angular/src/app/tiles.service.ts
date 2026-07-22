import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../environments/environment';
import {TileBounds, TileMetric, TilesResponse} from './tiles';

@Injectable({
    providedIn: 'root'
})
export class TilesService {

    private url = environment.apiUrl + '/tiles';

    constructor(private http: HttpClient) {
    }

    getTiles(metric: TileMetric, bounds?: TileBounds): Observable<TilesResponse> {
        return this.http.get<TilesResponse>(this.url, {params: this.params(metric, bounds)});
    }

    private params(metric: TileMetric, bounds?: TileBounds): HttpParams {
        let params = new HttpParams().set('metric', metric);
        if (bounds) {
            params = params
                .set('south', bounds.south)
                .set('north', bounds.north)
                .set('west', bounds.west)
                .set('east', bounds.east);
        }
        return params;
    }
}