// duration-util.service.ts
import {Injectable} from '@angular/core';

@Injectable({
    providedIn: 'root'
})
export class DurationUtilService {
    formatDuration(seconds: number): string {
        const hours = Math.floor(seconds / 3600);
        const minutes = Math.floor((seconds % 3600) / 60);
        return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')} h`;
    }
}