import {
    createTileRectangle,
    tileBorderColor,
    tileBorderWeight,
    tileMetricLabel,
    tileMetricValue,
    tileOverlayStyle,
    tileTooltip
} from './tile-overlay';
import {TILE_METRICS, TileLegend, TileSummary} from '../tiles';

function tile(overrides: Partial<TileSummary> = {}): TileSummary {
    return {
        tileId: '12:5',
        bounds: {south: 48.0, north: 48.005, west: 11.5, east: 11.51},
        firstActivityId: 1,
        latestActivityId: 3,
        maxPower: 250,
        maxHeartRate: 170,
        averagePower: 180,
        averageHeartRate: 140,
        activityIds: [1, 2, 3],
        ...overrides
    };
}

// The server names the metric by its Java enum constant, e.g. ACTIVITY_COUNT.
function legend(quantiles: number[], serverMetric = 'ACTIVITY_COUNT'): TileLegend {
    return {
        metric: serverMetric,
        min: quantiles.length > 0 ? quantiles[0] : null,
        max: quantiles.length > 0 ? quantiles[quantiles.length - 1] : null,
        quantiles
    };
}

describe('tileMetricValue', () => {
    it('reads the value of every metric from the tile', () => {
        const testee = tile();

        expect(tileMetricValue(testee, 'activityCount')).toBe(3);
        expect(tileMetricValue(testee, 'maxPower')).toBe(250);
        expect(tileMetricValue(testee, 'maxHeartRate')).toBe(170);
        expect(tileMetricValue(testee, 'averagePower')).toBe(180);
        expect(tileMetricValue(testee, 'averageHeartRate')).toBe(140);
    });

    it('returns null if the tile has no value for the metric', () => {
        const testee = tile({maxPower: null, averageHeartRate: null});

        expect(tileMetricValue(testee, 'maxPower')).toBeNull();
        expect(tileMetricValue(testee, 'averageHeartRate')).toBeNull();
    });
});

describe('tileMetricLabel', () => {
    it('has a label for every metric', () => {
        expect(TILE_METRICS.map(metric => tileMetricLabel(metric.value))).toEqual([
            'Activities', 'Max Power', 'Max Heart Rate', 'Average Power', 'Average Heart Rate'
        ]);
    });
});

describe('tileBorderColor', () => {
    const quantiles = [1, 2, 3];

    it('uses the first color for values up to the first quantile', () => {
        expect(tileBorderColor(0, legend(quantiles))).toBe('#2b3f8f');
        expect(tileBorderColor(1, legend(quantiles))).toBe('#2b3f8f');
    });

    it('uses the color of the first quantile that is greater than or equal to the value', () => {
        expect(tileBorderColor(1.5, legend(quantiles))).toBe('#2553a0');
        expect(tileBorderColor(3, legend(quantiles))).toBe('#1f67b0');
    });

    it('uses the last color for values beyond the last quantile', () => {
        expect(tileBorderColor(10, legend(quantiles))).toBe('#e63b2f');
    });

    it('uses the last color for the last of 20 quantiles', () => {
        const twentyQuantiles = Array.from({length: 20}, (_, index) => index + 1);

        expect(tileBorderColor(20, legend(twentyQuantiles))).toBe('#e63b2f');
        expect(tileBorderColor(10, legend(twentyQuantiles))).toBe('#5be08f');
    });

    it('uses the neutral color if the tile has no value', () => {
        expect(tileBorderColor(null, legend(quantiles))).toBe('#9fb3bf');
    });

    it('uses the neutral color if the legend has no quantiles', () => {
        expect(tileBorderColor(5, legend([]))).toBe('#9fb3bf');
    });
});

describe('tileBorderWeight', () => {
    it('is thinnest at or below zoom 6', () => {
        expect(tileBorderWeight(6, false)).toBe(2);
        expect(tileBorderWeight(2, false)).toBe(2);
    });

    it('grows linearly between zoom 6 and 16', () => {
        expect(tileBorderWeight(11, false)).toBeCloseTo(3.5);
    });

    it('is thickest at or above zoom 16', () => {
        expect(tileBorderWeight(16, false)).toBe(5);
        expect(tileBorderWeight(18, false)).toBe(5);
    });

    it('adds 2 for the selected tile', () => {
        expect(tileBorderWeight(6, true)).toBe(4);
        expect(tileBorderWeight(11, true)).toBeCloseTo(5.5);
        expect(tileBorderWeight(18, true)).toBe(7);
    });
});

describe('tileTooltip', () => {
    it('shows the activity count and the metric value with one decimal', () => {
        expect(tileTooltip(tile(), 'maxPower')).toBe('3 Activities • Max Power: 250.0');
        expect(tileTooltip(tile({averageHeartRate: 141.26}), 'averageHeartRate'))
            .toBe('3 Activities • Average Heart Rate: 141.3');
    });

    it('shows only the activity count if the tile has no value for the metric', () => {
        expect(tileTooltip(tile({maxPower: null}), 'maxPower')).toBe('3 Activities');
    });
});

describe('tileOverlayStyle', () => {
    it('draws a colored border with a transparent but painted fill', () => {
        const testee = tileOverlayStyle(tile(), legend([1, 2, 3]), 'activityCount', 11, false);

        expect(testee).toEqual({
            color: '#1f67b0',
            opacity: 0.6,
            weight: 3.5,
            fill: true,
            fillOpacity: 0,
            fillColor: 'transparent'
        });
    });

    it('emphasizes the selected tile', () => {
        const testee = tileOverlayStyle(tile(), legend([1, 2, 3]), 'activityCount', 11, true);

        expect(testee.opacity).toBe(0.9);
        expect(testee.weight).toBe(5.5);
    });

    it('colors the tile by the given metric', () => {
        const testee = tileOverlayStyle(tile({maxPower: null}), legend([100, 200], 'MAX_POWER'), 'maxPower', 11, false);

        expect(testee.color).toBe('#9fb3bf');
    });
});

describe('createTileRectangle', () => {
    it('spans the bounds of the tile', () => {
        const testee = createTileRectangle(tile(), 'activityCount', {});

        const bounds = testee.getBounds();
        expect(bounds.getSouth()).toBe(48.0);
        expect(bounds.getNorth()).toBe(48.005);
        expect(bounds.getWest()).toBe(11.5);
        expect(bounds.getEast()).toBe(11.51);
    });

    it('applies the given options', () => {
        const testee = createTileRectangle(tile(), 'activityCount', {color: '#123456', pane: 'tiles', bubblingMouseEvents: false});

        expect(testee.options.color).toBe('#123456');
        expect(testee.options.pane).toBe('tiles');
        expect(testee.options.bubblingMouseEvents).toBeFalse();
    });

    it('binds the tooltip of the tile', () => {
        const testee = createTileRectangle(tile(), 'maxPower', {});

        expect(testee.getTooltip()?.getContent()).toBe('3 Activities • Max Power: 250.0');
    });
});
