-- wrk JSON output script
-- Usage: wrk -s wrk_json.lua [options] <url>

local json = require "json"

-- Initialize counters
local requests = 0
local bytes = 0
local errors = {
   connect = 0,
   read = 0,
   write = 0,
   status = 0,
   timeout = 0
}

-- Track response times for latency calculations
local latencies = {}

-- Called for each request
function request()
   return wrk.format()
end

-- Called for each response
function response(status, headers, body)
   requests = requests + 1
   bytes = bytes + #body
   
   -- Store latency for later calculation
   table.insert(latencies, wrk.latency())
end

-- Called for connection errors
function error(error_type, error_message)
   if error_type == "connect" then
      errors.connect = errors.connect + 1
   elseif error_type == "read" then
      errors.read = errors.read + 1
   elseif error_type == "write" then
      errors.write = errors.write + 1
   elseif error_type == "status" then
      errors.status = errors.status + 1
   elseif error_type == "timeout" then
      errors.timeout = errors.timeout + 1
   end
end

-- Called when the test is complete
function done(summary, latency, requests)
   local duration = summary.duration / 1000000  -- Convert to seconds
   local rps = requests / duration
   
   -- Calculate latency percentiles
   local function percentile(data, p)
      if #data == 0 then return 0 end
      table.sort(data)
      local index = math.ceil((p / 100) * #data)
      return data[math.min(index, #data)]
   end
   
   local result = {
      duration = duration,
      requests = requests,
      bytes = bytes,
      requests_per_second = rps,
      bytes_per_second = bytes / duration,
      errors = errors,
      latency = {
         min = latency.min / 1000,  -- Convert to milliseconds
         max = latency.max / 1000,
         mean = latency.mean / 1000,
         stdev = latency.stdev / 1000,
         p50 = percentile(latencies, 50),
         p90 = percentile(latencies, 90),
         p95 = percentile(latencies, 95),
         p99 = percentile(latencies, 99)
      }
   }
   
   -- Output JSON to stdout
   print(json.encode(result))
end
