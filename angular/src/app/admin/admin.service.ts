import {Injectable} from '@angular/core';
import {Observable, throwError} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {catchError} from 'rxjs/operators';
import {environment} from '../../environments/environment';

export interface SystemInfo {
    applicationName: string;
    version: string;
    buildTime: string;
    javaVersion: string;
    gradleVersion: string;
    gitCommit: string;
    buildNumber: string;
    activeProfiles: string[];
    storageStats: {
        fitActivities: number;
        gpxActivities: number;
        totalActivities: number;
        storageSize: string;
    };
    memoryUsage: {
        used: string;
        total: string;
        max: string;
    };
}

export interface ImportResult {
    message: string;
    imported: number;
    skipped: number;
    errors: number;
}

export interface CacheResult {
    message: string;
    clearedEntries: number;
}

export interface AddedAndExistingTowns {
    added: number;
    existing: number;
}

export interface ChangePasswordRequest {
    currentPassword: string;
    newPassword: string;
}

export interface ChangePasswordResult {
    success: boolean;
    message: string;
}

export interface RebuildTilesResult {
    generatedAt: string | null;
    tiles: any[];
}

@Injectable({
    providedIn: 'root'
})
export class AdminService {
    private readonly baseUrl = `${environment.apiUrl}/admin`;

    constructor(private http: HttpClient) {
    }

    getSystemInfo(): Observable<SystemInfo> {
        return this.http.get<SystemInfo>(`${this.baseUrl}/system-info`)
            .pipe(
                catchError(this.handleError)
            );
    }

    importAllActivities(removeAllBeforeImport: boolean = false): Observable<ImportResult> {
        const params = removeAllBeforeImport ? '?removeAllBeforeImport=true' : '';
        return this.http.post<ImportResult>(`${this.baseUrl}/import-all${params}`, {})
            .pipe(
                catchError(this.handleError)
            );
    }

    clearCache(): Observable<CacheResult> {
        return this.http.post<CacheResult>(`${this.baseUrl}/clear-cache`, {})
            .pipe(
                catchError(this.handleError)
            );
    }

    addMissingTowns(): Observable<AddedAndExistingTowns> {
        return this.http.post<AddedAndExistingTowns>(`${this.baseUrl}/addMissingTowns`, {})
            .pipe(
                catchError(this.handleError)
            );
    }

    rebuildTiles(): Observable<RebuildTilesResult> {
        return this.http.post<RebuildTilesResult>(`${this.baseUrl}/rebuild-tiles`, {})
            .pipe(
                catchError(this.handleError)
            );
    }

    changePassword(request: ChangePasswordRequest): Observable<ChangePasswordResult> {
        return this.http.post<ChangePasswordResult>(`${this.baseUrl}/change-password`, request)
            .pipe(
                catchError(this.handleError)
            );
    }

    private handleError(error: any): Observable<never> {
        console.error('AdminService error:', error);
        return throwError(() => new Error(error.message || 'Server error'));
    }
}