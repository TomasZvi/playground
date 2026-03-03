# Playgrounds

## Task description
Create a playground REST API (use Spring Boot).
These are things that we expect library will do:
- define clear and usable domain classes, services, repository, controllers that should be used access required functionality and data
- API creates and manages play sites in playground. Play sites consists of attractions such as double swings, carousel, slide and a ball pit. 
  API allows to create different play sites with different combinations of attractions (for example API should enable one to add play site that consists of two swings, one slide and three ball pits and etc.)
- Attractions has maximum capacity like swings can have two kids maximum capacity
- API should expose endpoint that creates playsite with initial set of attractions, endpoint to edit playsite, get playsite info, delete playsite.
- API should expose endpoint that allows to add kid to particular playsite (we know kid's name, age, ticket number). Kids ticket number identifies kid uniquely. Api also should have endpoint to remove kid from playsite.
- API should not allow to add more kids to them than specified in configuration of play site (sum of all attractions capacity).
- API should automatically enqueue kid if playsite is full or receive negative result if kid does not accept waiting in queue. Or move waiting kid from queue to playsite if kid other was removed from playsite API register queues on play sites when tries to add kid to play site, that is full, and kid accepts waiting in queue).
- it should also be possible to remove kid from play site / queue
- API should provide play site utilization at current moment. Utilization is measured in %
- API should be able to provide a total visitor count during a current day on all play sites on the moment of request.
- It is not required to use DB or persistent data store, In-memory storage is enough.
  Please do not over-engineer, keep code simple and easy understandable.
  Create API as it would be used by playground manager from his usability perspective.
  If requirement or functionality is not clearly described please feel free implement it as you think would be logical. For example, “or receive negative result if kid does not accept waiting in queue “ does not say how kid decides to not accept waiting so it is for you to decide the rule let’s say 50/50 or some random. Similarly play site utilization calculation logic when play site has double swing together with other devices is up to you to decide.
  For sake of simplicity do not take into account synchronization of possible parallel data access/modification.
  Preferred shipment of homework is Github (or similar).