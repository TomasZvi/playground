# Playgrounds

## Task Description

Create a playground REST API (use Spring Boot).

### Expectations

We expect the library/application to:

- Define clear and usable domain classes, services, repositories, and controllers that provide access to the required functionality and data.
- Create and manage **play sites** in a playground. A play site consists of attractions such as double swings, carousel, slide, ball pit, etc.
- Allow creating different play sites with different combinations of attractions (for example, a play site with two swings, one slide, and three ball pits, etc.).
- Define attraction capacities (e.g., a double swing can have a maximum of two kids).
- Expose endpoints to:
  - Create a play site with an initial set of attractions
  - Edit a play site
  - Get play site information
  - Delete a play site
- Expose endpoints to manage kids on a play site:
  - Add a kid to a particular play site (kid details: name, age, ticket number)
  - Remove a kid from a play site
  - Kid’s ticket number uniquely identifies the kid
- Don't allow more kids in a play site than the play site capacity (the total capacity is the sum of all attraction capacities).
- When adding kid to a full play site:
  - Automatically enqueue the kid **if** the kid accepts waiting in the queue, **or**
  - Return a negative result if the kid does not accept waiting
  - When kid is removed from play site, move kids from the queue into the play site as space becomes available
- Make it possible to remove kid from play site **or** from the queue.
- Provide the current play site utilization. Utilization is measured as a percentage (%).
- Provide the total visitor count for the current day across all play sites at the moment of the request.
- Use in-memory storage only (a database or persistent data store is **not** required).

### Notes

- Please do not overengineer this; keep the code simple and easy to understand.
- Design the API as it would be used by a playground manager, focusing on usability.
- If a requirement or behavior is unclear, implement what you think is most logical.
  - For example, the requirement “return a negative result if the kid does not accept waiting in the queue” does not specify how the kid decides. You may choose a rule (e.g., random 50/50).
  - Similarly, utilization calculation logic for combinations like a double swing together with other attractions is up to you.
- For simplicity, do not account for synchronization/parallel access.
- Preferred submission is GitHub (or a similar service).