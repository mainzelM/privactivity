import {createTrackOverlayLabel} from './activities.component';

describe('createTrackOverlayLabel', () => {
    it('escapes the activity title before embedding it in HTML', () => {
        const label = createTrackOverlayLabel('#ff7800', 42, `Run <img src=x onerror="alert(1)"> & "friends"`);
        const parser = new DOMParser();
        const document = parser.parseFromString(label, 'text/html');

        expect(label).toContain('href="/activity-details/42"');
        expect(label).toContain('Run &lt;img src=x onerror=&quot;alert(1)&quot;&gt; &amp; &quot;friends&quot;');
        expect(document.querySelector('img')).toBeNull();
        expect(document.querySelector('a')?.textContent).toBe(`Run <img src=x onerror="alert(1)"> & "friends"`);
    });
});
