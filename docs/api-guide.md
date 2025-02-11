# API Integration Guide

## Data Sources

### OSRS Wiki Prices API

#### Latest Prices
```java
public class WikiPriceService {
    private static final String BASE_URL = 
        "https://prices.runescape.wiki/api/v1/osrs/latest";
    
    public PriceData getLatestPrice(int itemId) {
        String url = String.format("%s?id=%d", BASE_URL, itemId);
        return executeRequest(url);
    }
}
```

#### Time Series
```java
public class TimeSeriesService {
    private static final String BASE_URL = 
        "https://prices.runescape.wiki/api/v1/osrs/timeseries";
    
    public List<PricePoint> getHistory(int itemId, TimeFrame frame) {
        String url = String.format(
            "%s?id=%d&timestep=%s",
            BASE_URL, itemId, frame.getStep()
        );
        return executeRequest(url);
    }
}
```

#### Volume Data
```java
public class VolumeService {
    private static final String BASE_URL = 
        "https://prices.runescape.wiki/api/v1/osrs/volumes";
    
    public VolumeData getVolume(int itemId) {
        String url = String.format("%s?id=%d", BASE_URL, itemId);
        return executeRequest(url);
    }
}
```

### RuneLite API

#### GE Offers
```java
public class OfferTracker {
    @Subscribe
    public void onGrandExchangeOfferChanged(
            GrandExchangeOfferChanged event) {
        GrandExchangeOffer offer = event.getOffer();
        processOffer(offer);
    }
}
```

#### Item Info
```java
public class ItemService {
    private final ItemManager itemManager;
    
    public ItemData getItemInfo(int itemId) {
        ItemComposition comp = 
            itemManager.getItemComposition(itemId);
        return new ItemData(comp);
    }
}
```

## Data Models

### Price Models
```java
public class PriceData {
    private final int high;
    private final int low;
    private final long highTime;
    private final long lowTime;
    
    public int getSpread() {
        return high - low;
    }
    
    public double getMargin() {
        return (high - low) / (double) low;
    }
}

public class PricePoint {
    private final long timestamp;
    private final int price;
    private final int volume;
    
    public boolean isHighVolume(double avgVolume) {
        return volume > avgVolume * 1.5;
    }
}
```

### Analysis Models
```java
public class TechnicalIndicators {
    private final MACD macd;
    private final RSI rsi;
    private final ROC roc;
    
    public double getSignalStrength() {
        return (macd.getStrength() * 0.4) +
               (rsi.getStrength() * 0.3) +
               (roc.getStrength() * 0.3);
    }
}

public class MarketMetrics {
    private final double volume;
    private final double volatility;
    private final double trend;
    
    public RiskLevel getRiskLevel() {
        double risk = calculateRisk();
        return RiskLevel.fromValue(risk);
    }
}
```

## API Integration

### Request Handling
```java
public class ApiClient {
    private final OkHttpClient client;
    private final ObjectMapper mapper;
    
    public <T> T executeRequest(String url, Class<T> type) {
        Request request = new Request.Builder()
            .url(url)
            .header("User-Agent", "MarketMaster")
            .build();
            
        try (Response response = client.newCall(request).execute()) {
            return mapper.readValue(
                response.body().string(), type);
        }
    }
}
```

### Rate Limiting
```java
public class RateLimiter {
    private final int MAX_REQUESTS = 100;
    private final Duration WINDOW = Duration.ofMinutes(1);
    private final Queue<Instant> requests = new LinkedList<>();
    
    public synchronized boolean allowRequest() {
        Instant now = Instant.now();
        while (!requests.isEmpty() && 
               Duration.between(requests.peek(), now)
                   .compareTo(WINDOW) > 0) {
            requests.poll();
        }
        
        if (requests.size() < MAX_REQUESTS) {
            requests.offer(now);
            return true;
        }
        return false;
    }
}
```

### Caching
```java
public class PriceCache {
    private final Cache<Integer, PriceData> cache;
    private final Duration TTL = Duration.ofMinutes(5);
    
    public PriceCache() {
        this.cache = Caffeine.newBuilder()
            .expireAfterWrite(TTL)
            .maximumSize(1000)
            .build();
    }
    
    public PriceData get(int itemId) {
        return cache.get(itemId, this::fetchPrice);
    }
}
```

## Error Handling

### API Errors
```java
public class ApiException extends RuntimeException {
    private final ErrorType type;
    private final int code;
    
    public static ApiException from(Response response) {
        int code = response.code();
        ErrorType type = ErrorType.fromCode(code);
        return new ApiException(type, code);
    }
}

public enum ErrorType {
    RATE_LIMIT(429),
    SERVER_ERROR(500),
    NETWORK_ERROR(0);
    
    private final int code;
}
```

### Retry Logic
```java
public class RetryHandler {
    private final int maxRetries;
    private final Duration baseDelay;
    
    public <T> T executeWithRetry(Callable<T> task) {
        int attempts = 0;
        while (true) {
            try {
                return task.call();
            } catch (Exception e) {
                if (++attempts >= maxRetries) {
                    throw new RuntimeException(e);
                }
                sleep(calculateDelay(attempts));
            }
        }
    }
    
    private Duration calculateDelay(int attempt) {
        return baseDelay.multipliedBy(
            (long) Math.pow(2, attempt - 1));
    }
}
```

## Data Processing

### Price Analysis
```java
public class PriceAnalyzer {
    public PriceAnalysis analyze(List<PricePoint> prices) {
        double mean = calculateMean(prices);
        double stdDev = calculateStdDev(prices, mean);
        double trend = calculateTrend(prices);
        
        return new PriceAnalysis(mean, stdDev, trend);
    }
    
    private double calculateTrend(List<PricePoint> prices) {
        SimpleRegression regression = new SimpleRegression();
        for (int i = 0; i < prices.size(); i++) {
            regression.addData(i, prices.get(i).getPrice());
        }
        return regression.getSlope();
    }
}
```

### Volume Analysis
```java
public class VolumeAnalyzer {
    public VolumeMetrics analyze(List<TradeData> trades) {
        double volumeMA = calculateVolumeMA(trades);
        double volumeStdDev = calculateStdDev(trades);
        boolean isHighVolume = isVolumeAboveThreshold(trades);
        
        return new VolumeMetrics(
            volumeMA, volumeStdDev, isHighVolume);
    }
    
    private double calculateVolumeMA(List<TradeData> trades) {
        return trades.stream()
            .mapToInt(TradeData::getVolume)
            .average()
            .orElse(0);
    }
}
```

## Best Practices

### API Usage
1. **Rate Limiting**
   - Respect limits
   - Use caching
   - Batch requests
   - Handle errors

2. **Data Validation**
   - Check responses
   - Validate values
   - Handle nulls
   - Log errors

3. **Performance**
   - Cache data
   - Batch requests
   - Optimize calls
   - Monitor usage

### Error Handling
1. **Retry Strategy**
   - Exponential backoff
   - Max retries
   - Error logging
   - Fallback options

2. **Error Types**
   - Network errors
   - Rate limits
   - Server errors
   - Data errors

3. **Recovery**
   - Cache fallback
   - Default values
   - User notification
   - Error logging

## Testing

### Unit Tests
```java
public class ApiClientTest {
    @Test
    public void testPriceFetch() {
        ApiClient client = new ApiClient();
        PriceData data = client.getPrice(ITEM_ID);
        assertNotNull(data);
        assertTrue(data.getHigh() > 0);
        assertTrue(data.getLow() > 0);
    }
}
```

### Integration Tests
```java
public class ApiIntegrationTest {
    @Test
    public void testFullFlow() {
        PriceService service = new PriceService();
        List<PricePoint> history = 
            service.getHistory(ITEM_ID);
        PriceAnalysis analysis = 
            service.analyze(history);
        assertValidAnalysis(analysis);
    }
}
```

## Resources

### Documentation
- [OSRS Wiki API](https://oldschool.runescape.wiki/w/RuneScape:Real-time_Prices)
- [RuneLite API](https://static.runelite.net/api/runelite-api/)

### Libraries
- [OkHttp](https://square.github.io/okhttp/)
- [Jackson](https://github.com/FasterXML/jackson)
- [Caffeine](https://github.com/ben-manes/caffeine)

### Tools
- [Postman](https://www.postman.com/)
- [JSON Editor](https://jsoneditoronline.org/)
- [API Status](https://status.runescape.wiki/)