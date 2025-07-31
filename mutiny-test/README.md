https://terrakok.github.io/FlowMarbles/


Flow Marbles is an interactive visualization tool for Kotlin Flow operators, similar to RxMarbles for RxJS. It provides interactive diagrams of Kotlin Flow operations, helping developers understand how various Flow operators work through visual marble diagrams.

This is a similar interactive marble diagram visualization tool for Mutiny (the reactive streams library for Java). 
Here's what it includes:

## Key Features:

1. **Interactive Marble Diagrams**: Visual representation of reactive streams with animated marbles flowing through timelines

2. **Multiple Mutiny Operators**: 
   - `map` - Transform each item
   - `filter` - Only emit items that pass a test
   - `flatMap` - Transform and flatten streams
   - `merge` - Combine multiple streams
   - `zip` - Combine streams pairwise
   - `take` - Take first n items
   - `skip` - Skip first n items
   - `distinct` - Remove duplicates
   - `delay` - Add time delay
   - `scan` - Running accumulation

3. **Real-time Animation**: Watch how data flows through operators with smooth animations

4. **Interactive Elements**:
   - Click marbles to change their values
   - Add new marbles to the input stream
   - Play/pause animations
   - Reset the diagram

5. **Educational Content**: Each operator includes:
   - Clear description of functionality
   - Real Mutiny code examples
   - Visual representation of the transformation

6. **Modern UI**: Beautiful, responsive design with gradients, hover effects, and smooth animations

## How It Works:

The tool demonstrates how Mutiny's `Multi<T>` streams work by showing:
- **Input Stream**: Original data flowing through time
- **Operator**: The transformation being applied
- **Output Stream**: Resulting transformed data

This makes it easy to understand concepts like:
- How `map` transforms each element
- How `filter` removes items that don't match criteria
- How `flatMap` creates and flattens multiple streams
- How timing operators like `delay` affect emission timing

The visualization helps developers grasp reactive programming concepts intuitively, just like the original Flow Marbles does for Kotlin Flow, but specifically tailored for the Mutiny library used in Quarkus and other Java reactive applications.