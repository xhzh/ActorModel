package co.vaughnvernon.actormodel.agilepm.domain.model.product;

import java.util.Date;

import co.vaughnvernon.actormodel.actor.ActorAgent;
import co.vaughnvernon.actormodel.actor.ActorInitializer;
import co.vaughnvernon.actormodel.actor.ActorRegistry;
import co.vaughnvernon.actormodel.actor.ActorTestCase;
import co.vaughnvernon.actormodel.actor.Query;
import co.vaughnvernon.actormodel.agilepm.domain.model.product.backlogitem.BacklogItem;
import co.vaughnvernon.actormodel.agilepm.domain.model.product.backlogitem.CommitTo;
import co.vaughnvernon.actormodel.agilepm.domain.model.product.sprint.Sprint;
import co.vaughnvernon.actormodel.message.StringMessage;
import co.vaughnvernon.actormodel.stage.KeyValueStoreActorRegistry;
import co.vaughnvernon.actormodel.stage.address.UUIDActorAddressFactory;
import co.vaughnvernon.actormodel.stage.mailbox.local.LocalMailboxActorAgentFactory;
import co.vaughnvernon.actormodel.stage.mailbox.local.LocalMailboxFactory;
import co.vaughnvernon.actormodel.stage.persistence.hashmap.HashMapKeyValueStoreFactory;

public class ProductTest extends ActorTestCase {

	private ActorRegistry actorRegistry;

	public ProductTest() {
		super();

		this.actorRegistry =
				KeyValueStoreActorRegistry
					.instance(
							new UUIDActorAddressFactory(),
							new LocalMailboxActorAgentFactory(),
							new LocalMailboxFactory(),
							new HashMapKeyValueStoreFactory());
	}

	public void testProductPlanBacklogItem() throws Exception {
		ActorInitializer initializer = new ActorInitializer(this.actorRegistry);
		initializer.putParameter("name", "Test Product");
		initializer.putParameter("description", "A decsription.");

		ActorAgent product = this.actorRegistry.actorFor(Product.class, initializer);

		product.tell(new PlanBacklogItem(
				"A summary of the backlog item.",
				"A story about the backlog item."));

		this.stayAlive(500L);
	}

	public void testProductScheduleSprint() throws Exception {
		ActorInitializer initializer = new ActorInitializer(this.actorRegistry);
		initializer.putParameter("name", "Test Product");
		initializer.putParameter("description", "A decsription.");

		ActorAgent product = this.actorRegistry.actorFor(Product.class, initializer);

		product.tell(new ScheduleSprint(
				"Sprint 1",
				"The initial sprint.",
				this.daysFromNow(1),
				this.daysFromNow(15)));

		this.stayAlive(500L);
	}

	public void testCommitBacklogItemToSprint() throws Exception {
		ActorInitializer initializer = new ActorInitializer(this.actorRegistry);
		initializer.putParameter("name", "Test Product");
		initializer.putParameter("description", "A decsription.");

		final ActorAgent product = this.actorRegistry.actorFor(Product.class, initializer);

		product.tell(new PlanBacklogItem(
				"A summary of the backlog item.",
				"A story about the backlog item."));

		product.tell(new ScheduleSprint(
				"Sprint 1",
				"The initial sprint.",
				this.daysFromNow(1),
				this.daysFromNow(15)));

		this.stayAlive(100L);

		ActorAgent sprint =
				this.actorRegistry.findFirstMatching(
						Sprint.class,
						new Query("name", "Sprint 1"));

		assertNotNull(sprint);

		this.stayAlive(100L);

		ActorAgent backlogItem =
				this.actorRegistry.findFirstMatching(
						BacklogItem.class,
						new Query("summary", "A summary of the backlog item."));

		assertNotNull(backlogItem);

		backlogItem.tell(new CommitTo(sprint.address()));

		this.stayAlive(500L);

		backlogItem.tell(new StringMessage("Echo"));

		sprint.tell(new StringMessage("Echo"));

		this.stayAlive(100L);
	}

	private Date daysFromNow(long aNumberOfDays) {
		Date now = new Date();

		return new Date(now.getTime() + (aNumberOfDays * 24L * 60L * 60L * 1000L));
	}
}
